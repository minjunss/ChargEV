package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargerType;
import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.domain.Stat;
import ChargEV.ChargEV.chargingStation.domain.ZCode;
import ChargEV.ChargEV.feignClient.GongGongClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class StationDataFetcher {

    @Value("${gonggong.serviceCode}")
    private String serviceCode;
    private final GongGongClient gongGongClient;
    private final Gson gson;

    /**
     * 특정 지역(zcode)의 모든 충전소 정보를 공공 API를 통해 가져와 파싱합니다.
     * @param zcode 지역 코드
     * @return 파싱된 ChargingStation 객체 리스트
     */
    public List<ChargingStation> fetchDataByRegion(ZCode zcode) {
        List<ChargingStation> fetchedStations = new ArrayList<>();
        int page = 1;
        boolean hasNextPage;

        log.info("API CALL for: {} - {}", zcode.getCode(), zcode.getRegion());
        do {
            try {
                String responseBody = gongGongClient.getChargerInfo(serviceCode, page, 9999, zcode.getCode(), "JSON");
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                JsonArray chargingStationsArray = Optional.ofNullable(jsonObject.getAsJsonObject("items"))
                        .map(obj -> obj.getAsJsonArray("item"))
                        .orElse(new JsonArray());

                hasNextPage = !chargingStationsArray.isEmpty();

                for (JsonElement element : chargingStationsArray) {
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
                    fetchedStations.add(chargingStation);
                }
                page++;
            } catch (Exception e) {
                log.error("API 호출 중 오류 발생 (zcode: {}, page: {})", zcode, page, e);
                break;
            }
        } while (hasNextPage);

        return fetchedStations;
    }
}
