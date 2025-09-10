package ChargEV.ChargEV.chargingStation.repository;

import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;

import java.util.List;

public interface ChargingStationRepositoryCustom {
    List<ChargingStationResDto> findByCoordinates(ChargingStationByRangeReqDto reqDto);
    List<ChargingStationDetailResDto> findDetailByStatId(String statId);
}
