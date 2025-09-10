package ChargEV.ChargEV.chargingStation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChargingStationByRangeReqDto {
    @Schema(description = "최소 위도값", example = "37.563431")
    @NotBlank(message = "최소 위도값은 필수입니다.")
    private Double minLatitude;
    @Schema(description = "최대 위도값", example = "37.963431")
    @NotBlank(message = "최대 위도값은 필수입니다.")
    private Double maxLatitude;
    @Schema(description = "최소 경도값", example = "126.563431")
    @NotBlank(message = "최소 경도값은 필수입니다.")
    private Double minLongitude;
    @Schema(description = "최대 경도값", example = "127.234521")
    @NotBlank(message = "최대 경도값은 필수입니다.")
    private Double maxLongitude;

    @Schema(description = "충전기 타입 필터 리스트", example = "[\"01\", \"04\"]")
    private List<String> chargerTypes;
}
