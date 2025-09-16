package ChargEV.ChargEV.member.dto;

import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResDto {
    private Long id;
    private ChargingStationResDto chargingStation;
}
