package com.ChargEV.ChargEV.chargingStation.repository;

import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ChargingStationRepositoryCustom {
    List<ChargingStationResDto> findByCoordinates(ChargingStationByRangeReqDto reqDto);
}
