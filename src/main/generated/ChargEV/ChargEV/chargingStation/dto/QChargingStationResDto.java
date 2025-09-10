package ChargEV.ChargEV.chargingStation.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * ChargEV.ChargEV.chargingStation.dto.QChargingStationResDto is a Querydsl Projection type for ChargingStationResDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QChargingStationResDto extends ConstructorExpression<ChargingStationResDto> {

    private static final long serialVersionUID = -1121642672L;

    public QChargingStationResDto(com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<String> statId, com.querydsl.core.types.Expression<String> address, com.querydsl.core.types.Expression<String> location, com.querydsl.core.types.Expression<Double> latitude, com.querydsl.core.types.Expression<String> note, com.querydsl.core.types.Expression<String> limitYn, com.querydsl.core.types.Expression<String> limitDetail, com.querydsl.core.types.Expression<Double> longitude, com.querydsl.core.types.Expression<String> useTime, com.querydsl.core.types.Expression<String> updatedDate, com.querydsl.core.types.Expression<String> stat) {
        super(ChargingStationResDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, double.class, String.class, String.class, String.class, double.class, String.class, String.class, String.class}, name, statId, address, location, latitude, note, limitYn, limitDetail, longitude, useTime, updatedDate, stat);
    }

}

