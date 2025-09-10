package ChargEV.ChargEV.chargingStation.controller;

import ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "충전소 정보 업데이트", description = "충전소 정보 업데이트")
    @GetMapping("/fetch")
    public ResponseEntity<?> fetch() {
        chargingStationService.updateChargingStation();
        return ResponseEntity.ok().build();
    }
}
