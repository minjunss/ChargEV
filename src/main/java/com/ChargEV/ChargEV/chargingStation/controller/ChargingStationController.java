package com.ChargEV.ChargEV.chargingStation.controller;

import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import com.ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import com.ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chargingStation")
@Slf4j
@RequiredArgsConstructor
public class ChargingStationController {
    private final ChargingStationService chargingStationService;

    @GetMapping("/fetch")
    public ResponseEntity<Void> fetch() {
        chargingStationService.updateChargingStations();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/range")
    public ResponseEntity<List<ChargingStationResDto>> getChargingStationsByRange(@RequestBody ChargingStationByRangeReqDto reqDto) {
        return ResponseEntity.ok(chargingStationService.getChargingStationsByRange(reqDto));
    }
}
