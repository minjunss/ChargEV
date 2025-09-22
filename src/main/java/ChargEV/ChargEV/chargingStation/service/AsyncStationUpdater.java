package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.domain.ZCode;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncStationUpdater {

    private final StationDataFetcher stationDataFetcher;
    private final ChargingStationRepository chargingStationRepository;

    @Async
    @Transactional
    public void updateStationData(String statId, ZCode zcode) {
        log.info("비동기: 실시간 정보 업데이트 시작. statId: {}, zcode: {}", statId, zcode.getCode());
        try {
            List<ChargingStation> liveStations = stationDataFetcher.fetchDataByRegion(zcode);

            List<ChargingStation> relevantLiveStations = liveStations.stream()
                    .filter(s -> s.getStatId().equals(statId))
                    .toList();

            if (relevantLiveStations.isEmpty()) {
                log.warn("비동기: API 호출 결과에 해당 statId가 없습니다. statId: {}", statId);
                return;
            }

            List<ChargingStation> stationsInDB = chargingStationRepository.findByStatId(statId);
            var liveStationsMap = relevantLiveStations.stream()
                    .collect(Collectors.toMap(ChargingStation::getChargerId, s -> s));

            List<ChargingStation> stationsToUpdate = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            for (ChargingStation existingStation : stationsInDB) {
                ChargingStation liveData = liveStationsMap.get(existingStation.getChargerId());
                if (liveData != null) {
                    try {
                        String liveUpdatedDateStr = liveData.getUpdatedDate();
                        if (liveUpdatedDateStr != null && !liveUpdatedDateStr.isEmpty() &&
                            (existingStation.getUpdatedDate() == null || existingStation.getUpdatedDate().isEmpty() ||
                             LocalDateTime.parse(liveUpdatedDateStr, formatter).isAfter(LocalDateTime.parse(existingStation.getUpdatedDate(), formatter)))) {

                            existingStation.update(
                                liveData.getName(), liveData.getStatId(), liveData.getChargerId(), liveData.getChargerType(),
                                liveData.getAddress(), liveData.getLocation(), liveData.getLatitude(), liveData.getNote(),
                                liveData.getLimitYn(), liveData.getLimitDetail(), liveData.getLongitude(), liveData.getUseTime(),
                                liveData.getStat(), liveData.getOutput(), liveData.getMethod(), liveData.getKind(),
                                liveData.getKindDetail(), liveData.getZcode(), liveData.getUpdatedDate(), liveData.getDelYn()
                            );
                            stationsToUpdate.add(existingStation);
                        }
                    } catch (Exception e) {
                        log.warn("비동기: 데이터 파싱 또는 날짜 비교 중 오류. statId: {}, chargerId: {}", statId, existingStation.getChargerId(), e);
                    }
                }
            }

            if (!stationsToUpdate.isEmpty()) {
                chargingStationRepository.saveAll(stationsToUpdate);
                log.info("비동기: {}개의 충전기 정보 업데이트 완료. statId: {}", stationsToUpdate.size(), statId);
                // TODO: WebSocket을 클라이언트에 업데이트 정보 보내기
            }

        } catch (Exception e) {
            log.error("비동기: 충전소 정보 업데이트 중 심각한 오류 발생. statId: {}", statId, e);
        }
    }
}
