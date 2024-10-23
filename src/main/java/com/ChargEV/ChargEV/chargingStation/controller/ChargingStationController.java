package com.ChargEV.ChargEV.chargingStation.controller;

import com.ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chargingStation")
@Slf4j
@RequiredArgsConstructor
public class ChargingStationController {
    private final ChargingStationService chargingStationService;

    @GetMapping("/fetch")
    public ResponseEntity fetch() {
        chargingStationService.updateChargingStations();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/range")
    public ResponseEntity getChargingStationsByRange() {
        return ResponseEntity.ok().build();
    }
}
