package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargerType;
import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.domain.Stat;
import ChargEV.ChargEV.chargingStation.domain.ZCode;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.feignClient.GongGongClient;
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

    @Transactional
    public List<ChargingStationDetailResDto> getDetail(String statId) {
        List<ChargingStation> stationsInDB = chargingStationRepository.findByStatId(statId);
        if (stationsInDB.isEmpty()) {
            throw new NoSuchElementException("해당 충전소 정보를 찾을 수 없습니다: " + statId);
        }
        log.info("실시간 정보 조회를 시작합니다. statId: {}", statId);

        ZCode zCode = stationsInDB.getFirst().getZcode();
        try {
            String responseBody = gongGongClient.getChargerInfoByStatId(serviceCode, 1, 100, zCode.getCode(), statId, "JSON");
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonArray items = Optional.ofNullable(jsonObject.getAsJsonObject("items"))
                                     .map(obj -> obj.getAsJsonArray("item"))
                                     .orElse(new JsonArray());

            // statId를 가진 충전기들만 필터링
            Map<String, JsonObject> liveDataMap = new HashMap<>();
            for (JsonElement item : items) {
                JsonObject obj = item.getAsJsonObject();
                if (obj.get("statId").getAsString().equals(statId)) {
                    liveDataMap.put(obj.get("chgerId").getAsString(), obj);
                }
            }

            // 기존 충전기 정보와 비교하여 업데이트
            if (!liveDataMap.isEmpty()) {
                List<ChargingStation> stationsToUpdate = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                for (ChargingStation existingStation : stationsInDB) {
                    JsonObject liveData = liveDataMap.get(existingStation.getChargerId());
                    if (liveData != null) {
                        // 업데이트 날짜를 비교하여 최신 정보일 경우에만 업데이트
                        try {
                            String liveUpdatedDateStr = liveData.get("statUpdDt").getAsString();
                            if (liveUpdatedDateStr != null && !liveUpdatedDateStr.isEmpty() &&
                                (existingStation.getUpdatedDate() == null || existingStation.getUpdatedDate().isEmpty() ||
                                 LocalDateTime.parse(liveUpdatedDateStr, formatter).isAfter(LocalDateTime.parse(existingStation.getUpdatedDate(), formatter)))) {

                                existingStation.update(
                                    liveData.get("statNm").getAsString(),
                                    liveData.get("statId").getAsString(),
                                    liveData.get("chgerId").getAsString(),
                                    ChargerType.fromCode(liveData.get("chgerType").getAsString()),
                                    liveData.get("addr").getAsString(),
                                    liveData.get("location").getAsString(),
                                    liveData.get("lat").getAsDouble(),
                                    liveData.get("note").getAsString(),
                                    liveData.get("limitYn").getAsString(),
                                    liveData.get("limitDetail").getAsString(),
                                    liveData.get("lng").getAsDouble(),
                                    liveData.get("useTime").getAsString(),
                                    Stat.fromCode(liveData.get("stat").getAsString()),
                                    liveData.get("output").getAsString(),
                                    liveData.get("method").getAsString(),
                                    liveData.get("kind").getAsString(),
                                    liveData.get("kindDetail").getAsString(),
                                    ZCode.fromCode(liveData.get("zcode").getAsInt()),
                                    liveUpdatedDateStr,
                                    liveData.get("delYn").getAsString()
                                );
                                stationsToUpdate.add(existingStation);
                            }
                        } catch (Exception e) {
                            log.warn("실시간 데이터 파싱 또는 날짜 비교 중 오류 발생 (statId: {}, chargerId: {})", existingStation.getStatId(), existingStation.getChargerId(), e);
                        }
                    }
                }
                if (!stationsToUpdate.isEmpty()) {
                    chargingStationRepository.saveAll(stationsToUpdate);
                    log.info("{}개의 충전기 실시간 정보 업데이트 완료. statId: {}", stationsToUpdate.size(), statId);
                }
            }
        } catch (Exception e) {
            log.error("공공 API 호출 또는 데이터 처리 중 심각한 오류 발생. statId: {}", statId, e);
            // API 호출 실패 시에도 DB에 저장된 기존 데이터를 반환
        }

        // DB에서 다시 조회하여 반환
        return chargingStationRepository.findDetailByStatId(statId);
    }
}
