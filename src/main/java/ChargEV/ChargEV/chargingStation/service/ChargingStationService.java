package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.domain.ZCode;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;
    private final StationDataFetcher stationDataFetcher;
    private final AsyncStationUpdater asyncStationUpdater;

    @Async
    @Scheduled(fixedRate = 300000) // 5분
    @Transactional
    public void updateChargingStation() {
        log.info("전체 충전소 데이터 업데이트 시작");

        List<ChargingStation> stationsToFetch = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        try {
            for (ZCode zcode : ZCode.values()) {
                Future<?> future = executorService.submit(() -> {
                    List<ChargingStation> fetched = stationDataFetcher.fetchDataByRegion(zcode);
                    synchronized (stationsToFetch) {
                        stationsToFetch.addAll(fetched);
                    }
                });
                futures.add(future);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("스레드 작업 중 오류 발생", e);
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("작업 대기 중 인터럽트 발생", e);
                executorService.shutdown();
                Thread.currentThread().interrupt();
            }
        }

        List<ChargingStation> existingStations = chargingStationRepository.findAll();
        Map<String, ChargingStation> existingMap = existingStations.stream()
                .collect(Collectors.toMap(
                        s -> s.getStatId() + "-" + s.getChargerId(),
                        s -> s,
                        (existing, replacement) -> replacement
                ));

        List<ChargingStation> stationToInsert = new ArrayList<>();
        List<ChargingStation> stationToUpdate = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        for (ChargingStation station : stationsToFetch) {
            String key = station.getStatId() + "-" + station.getChargerId();
            ChargingStation existing = existingMap.get(key);

            if (existing != null) {
                try {
                    if (station.getUpdatedDate() != null && !station.getUpdatedDate().isEmpty() &&
                        (existing.getUpdatedDate() == null || existing.getUpdatedDate().isEmpty() ||
                         LocalDateTime.parse(station.getUpdatedDate(), formatter).isAfter(LocalDateTime.parse(existing.getUpdatedDate(), formatter)))) {
                        
                        existing.update(
                            station.getName(), station.getStatId(), station.getChargerId(), station.getChargerType(),
                            station.getAddress(), station.getLocation(), station.getLatitude(), station.getNote(),
                            station.getLimitYn(), station.getLimitDetail(), station.getLongitude(), station.getUseTime(),
                            station.getStat(), station.getOutput(), station.getMethod(), station.getKind(),
                            station.getKindDetail(), station.getZcode(), station.getUpdatedDate(), station.getDelYn()
                        );
                        stationToUpdate.add(existing);
                    }
                } catch (Exception e) {
                    log.warn("날짜 파싱 오류 (key: {})", key, e);
                }
            } else {
                stationToInsert.add(station);
            }
        }

        chargingStationRepository.saveAll(stationToInsert);
        chargingStationRepository.saveAll(stationToUpdate);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info("충전소 데이터 업데이트 완료: 저장 {}개, 업데이트 {}개", stationToInsert.size(), stationToUpdate.size());
            }
        });
    }

    public List<ChargingStationResDto> getChargingStationsByRange(ChargingStationByRangeReqDto reqDto) {
        return chargingStationRepository.findByCoordinates(reqDto);
    }

    public List<ChargingStationDetailResDto> getDetail(String statId) {
        log.info("상세 정보 조회 시작. statId: {}", statId);
        List<ChargingStationDetailResDto> currentDetails = chargingStationRepository.findDetailByStatId(statId);
        if (currentDetails.isEmpty()) {
            throw new NoSuchElementException("해당 충전소 정보를 찾을 수 없습니다: " + statId);
        }

        chargingStationRepository.findByStatId(statId).stream().findFirst().ifPresent(station -> {
            asyncStationUpdater.updateStationData(statId, station.getZcode());
        });

        return currentDetails;
    }
}