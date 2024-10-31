package com.ChargEV.ChargEV.chargingStation.service;

import com.ChargEV.ChargEV.chargingStation.domain.ChargerType;
import com.ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import com.ChargEV.ChargEV.chargingStation.domain.Stat;
import com.ChargEV.ChargEV.chargingStation.domain.ZCode;
import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import com.ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import com.ChargEV.ChargEV.feignClient.client.GongGongClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChargingStationService {
    @Value("${gonggong.serviceCode}")
    private String serviceCode;
    private final Gson gson;
    private final GongGongClient gongGongClient;
    private final ChargingStationRepository chargingStationRepository;



    @Transactional
    public void updateChargingStations() {
        fetchChargingStation();
    }


    private void fetchChargingStation() {
        log.info("충전소 데이터 패치 시작");
        List<ChargingStation> stationsToSave;
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            ZCode[] zcodes = ZCode.values();

            // 모든 충전소를 메모리에 저장
            stationsToSave = new ArrayList<>();

            for (ZCode zcode : zcodes) {
                executorService.submit(() -> {
                    log.info("API CALL for: {} - {}", zcode.getCode(), zcode.getRegion());
                    int page = 1;
                    boolean hasNextPage;

                    do {
                        String responseBody;
                        try {
                            responseBody = gongGongClient.getChargerInfo(serviceCode, page, 9999, zcode.getCode(), "JSON"); //api 호출
                            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class); //json 형태로 변환
                            JsonArray chargingStations = jsonObject
                                    .getAsJsonObject("items")
                                    .getAsJsonArray("item"); //item 추출

                            hasNextPage = !chargingStations.isEmpty();

                            //각 element들을 entity로 변환, list에 추가
                            for (JsonElement element : chargingStations) {
                                JsonObject stationObject = element.getAsJsonObject();

                                ChargingStation chargingStation = ChargingStation.builder()
                                        .name(stationObject.get("statNm").getAsString())
                                        .statId(stationObject.get("statId").getAsString())
                                        .chargerId(stationObject.get("chgerId").getAsString())
                                        .chargerType(ChargerType.fromCode(stationObject.get("chgerType").getAsString()))
                                        .kind(stationObject.get("kind").getAsString())
                                        .kindDetail(stationObject.get("kindDetail").getAsString())
                                        .limitYn(stationObject.get("limitYn").getAsString())
                                        .limitDetail(stationObject.get("limitDetail").getAsString())
                                        .location(stationObject.get("location").getAsString())
                                        .method(stationObject.get("method").getAsString())
                                        .note(stationObject.get("note").getAsString())
                                        .address(stationObject.get("addr").getAsString())
                                        .latitude(stationObject.get("lat").getAsDouble())
                                        .longitude(stationObject.get("lng").getAsDouble())
                                        .useTime(stationObject.get("useTime").getAsString())
                                        .stat(Stat.fromCode(stationObject.get("stat").getAsString()))
                                        .output(stationObject.get("output").getAsString())
                                        .method(stationObject.get("method").getAsString())
                                        .zcode(ZCode.fromCode(stationObject.get("zcode").getAsInt()))
                                        .updatedDate(stationObject.get("statUpdDt").getAsString())
                                        .delYn(stationObject.get("delYn").getAsString())
                                        .build();

                                stationsToSave.add(chargingStation);
                            }

                            page++;
                        } catch (Exception e) {
                            log.error("API 호출 중 오류 발생", e);
                            break;
                        }
                    } while (hasNextPage);
                });
            }
            executorService.shutdown();

            try {
                // 모든 작업이 완료될 때까지 대기
                if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                    log.warn("API 작업이 지정된 시간 내에 완료되지 않았습니다.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("작업 대기 중 인터럽트 발생", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }

        }
        // 데이터베이스에서 기존 충전소를 모두 가져옴
        List<ChargingStation> existingStations = new ArrayList<>();

        int batchSize = 10000; // 배치 크기

        // 'stationsToSave' 리스트를 batchSize 만큼 나누어 데이터베이스에서 충전소를 가져오는 과정
        for (int i = 0; i < stationsToSave.size(); i += batchSize) {
            List<String> statIdsBatch = stationsToSave.subList(i, Math.min(i + batchSize, stationsToSave.size())) // sublist로 자르기
                    .stream()
                    .map(ChargingStation::getStatId) // 각 ChargingStation 객체에서 statId를 가져옴
                    .collect(Collectors.toList()); // statId들을 리스트로

            List<String> chargerIdsBatch = stationsToSave.subList(i, Math.min(i + batchSize, stationsToSave.size()))
                    .stream()
                    .map(ChargingStation::getChargerId) // 각 ChargingStation 객체에서 chargerId를 가져옴
                    .collect(Collectors.toList()); // chargerId들을 리스트로

            //현재 배치의 statId, chargerId 들의 현재 데이터베이스 조회, 결과 추가
            existingStations.addAll(chargingStationRepository
                    .findAllByStatIdInAndChargerIdIn(statIdsBatch, chargerIdsBatch));
        }

        // 기존 충전소 목록을 Map 형태로 변환
        Map<String, ChargingStation> existingStationsMap = existingStations.stream()
                .collect(Collectors.toMap(
                        station -> station.getStatId() + "-" + station.getChargerId(),
                        station -> station,
                        (existing, replacement) -> replacement
                ));

        List<ChargingStation> stationsToInsert = new ArrayList<>();

        // 업데이트할 충전소를 결정
        for (ChargingStation station : stationsToSave) {
            String key = station.getStatId() + "-" + station.getChargerId();
            if (existingStationsMap.containsKey(key)) {
                // 기존 충전소가 존재하는 경우
                ChargingStation existingStation = existingStationsMap.get(key);
                // update
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                if (!station.getUpdatedDate().isEmpty() && !existingStation.getUpdatedDate().isEmpty()
                        && LocalDateTime.parse(station.getUpdatedDate(), formatter).isAfter(LocalDateTime.parse(existingStation.getUpdatedDate(), formatter))) {
                    existingStation.update(
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
                }
            } else {
                // 새로운 충전소를 추가
                stationsToInsert.add(station);
            }
        }

        // 배치 업데이트 및 삽입
        chargingStationRepository.saveAll(stationsToInsert);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info("충전소 데이터 패치 완료");
            }
        });
    }

    public List<ChargingStationResDto> getChargingStationsByRange(ChargingStationByRangeReqDto reqDto) {
        return chargingStationRepository.findByCoordinates(reqDto);
    }

}
