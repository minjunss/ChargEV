package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargerType;
import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.domain.Stat;
import ChargEV.ChargEV.chargingStation.domain.ZCode;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.feignClient.GongGongClient;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChargingStationService {
    @Value("${gonggong.serviceCode}")
    private String serviceCode;
    private final ChargingStationRepository chargingStationRepository;
    private final GongGongClient gongGongClient;
    private final Gson gson;

    @Transactional
    public void updateChargingStation() {
        log.info("충전소 데이터 패치 시작");

        List<ChargingStation> stationsToFetch = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        try {
            for (ZCode zcode : ZCode.values()) {
                Future<?> future = executorService.submit(() -> {
                    log.info("API CALL for: {} - {}", zcode.getCode(), zcode.getRegion());

                    int page = 1;
                    boolean hasNextPage;

                    do {
                        try {
                            String responseBody = gongGongClient.getChargerInfo(serviceCode, page, 9999, zcode.getCode(), "JSON");
                            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                            JsonArray chargingStations = Optional.ofNullable(jsonObject.getAsJsonObject("items"))
                                    .map(obj -> obj.getAsJsonArray("item"))
                                    .orElse(new JsonArray());

                            hasNextPage = !chargingStations.isEmpty();

                            for (JsonElement element : chargingStations) {
                                JsonObject obj = element.getAsJsonObject();

                                ChargingStation chargingStation = ChargingStation.builder()
                                        .name(obj.get("statNm").getAsString())
                                        .statId(obj.get("statId").getAsString())
                                        .chargerId(obj.get("chgerId").getAsString())
                                        .chargerType(ChargerType.fromCode(obj.get("chgerType").getAsString()))
                                        .kind(obj.get("kind").getAsString())
                                        .kindDetail(obj.get("kindDetail").getAsString())
                                        .limitYn(obj.get("limitYn").getAsString())
                                        .limitDetail(obj.get("limitDetail").getAsString())
                                        .location(obj.get("location").getAsString())
                                        .method(obj.get("method").getAsString())
                                        .note(obj.get("note").getAsString())
                                        .address(obj.get("addr").getAsString())
                                        .latitude(obj.get("lat").getAsDouble())
                                        .longitude(obj.get("lng").getAsDouble())
                                        .useTime(obj.get("useTime").getAsString())
                                        .stat(Stat.fromCode(obj.get("stat").getAsString()))
                                        .output(obj.get("output").getAsString())
                                        .zcode(ZCode.fromCode(obj.get("zcode").getAsInt()))
                                        .updatedDate(obj.get("statUpdDt").getAsString())
                                        .delYn(obj.get("delYn").getAsString())
                                        .build();

                                stationsToFetch.add(chargingStation);
                            }

                            page++;
                        } catch (Exception e) {
                            log.error("API 호출 중 오류 발생 (zcode: {}, page: {})", zcode, page, e);
                            break;
                        }
                    }
                    while (hasNextPage);
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

        //기존 충전소 조회
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

            if (existingMap.containsKey(key)) {
                ChargingStation existing = existingMap.get(key);

                boolean shouldUpdate = false;
                try {
                    if (!station.getUpdatedDate().isEmpty() && !existing.getUpdatedDate().isEmpty()) {
                        LocalDateTime newUpdated = LocalDateTime.parse(station.getUpdatedDate(), formatter);
                        LocalDateTime oldUpdated = LocalDateTime.parse(existing.getUpdatedDate(), formatter);
                        shouldUpdate = newUpdated.isAfter(oldUpdated);
                    }
                } catch (Exception e) {
                    log.warn("날짜 파싱 오류 (key: {})", key, e);
                }

                if (shouldUpdate) {
                    existing.update(
                            station.getName(),
                            station.getStatId(),
                            station.getChargerId(),
                            station.getChargerType(),
                            station.getAddress(),
                            station.getLocation(),
                            station.getLatitude(),
                            station.getNote(),
                            station.getLimitYn(),
                            station.getLimitDetail(),
                            station.getLongitude(),
                            station.getUseTime(),
                            station.getStat(),
                            station.getOutput(),
                            station.getMethod(),
                            station.getKind(),
                            station.getKindDetail(),
                            station.getZcode(),
                            station.getUpdatedDate(),
                            station.getDelYn()
                    );
                    stationToUpdate.add(existing);
                }
            } else {
                stationToInsert.add(station);
            }
        }

        //저장 및 업데이트
        chargingStationRepository.saveAll(stationToInsert);
        chargingStationRepository.saveAll(stationToUpdate);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info("충전소 데이터 패치 완료: 저장 {}개, 업데이트 {}개", stationToInsert.size(), stationToUpdate.size());
            }
        });
    }

    public List<ChargingStationResDto> getChargingStationsByRange(ChargingStationByRangeReqDto reqDto) {
        return chargingStationRepository.findByCoordinates(reqDto);
    }

    public List<ChargingStationDetailResDto> getDetail(String statId) {
        return chargingStationRepository.findDetailByStatId(statId);
    }
}
