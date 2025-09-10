package ChargEV.ChargEV.chargingStation.controller;

import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chargingStation")
@Slf4j
@RequiredArgsConstructor
public class ChargingStationController {
    private final ChargingStationService chargingStationService;

    @Operation(summary = "충전소 정보 업데이트", description = "충전소 정보 업데이트")
    @GetMapping("/fetch")
    public ResponseEntity<Void> fetch() {
        chargingStationService.updateChargingStation();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "범위 안 충전소 조회",description = "위도, 경도로 범위 안 충전소 조회")
    @PostMapping("/range")
    public ResponseEntity<List<ChargingStationResDto>> range(@RequestBody ChargingStationByRangeReqDto reqDto) {
        return ResponseEntity.ok(chargingStationService.getChargingStationsByRange(reqDto));
    }

    @Operation(summary = "충전소 상세정보 조회", description = "충전소ID로 충전소 상세정보 조회")
    @GetMapping("/detail")
    public ResponseEntity<List<ChargingStationDetailResDto>> getChargingStationDetail(@RequestParam("statId") String statId) {
        List<ChargingStationDetailResDto> response = chargingStationService.getDetail(statId);
        return ResponseEntity.ok(response);
    }
}
