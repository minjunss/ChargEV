package ChargEV.ChargEV.chargingStation.repository;

import ChargEV.ChargEV.chargingStation.dto.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static ChargEV.ChargEV.chargingStation.domain.QChargingStation.chargingStation;

@RequiredArgsConstructor
public class ChargingStationRepositoryCustomImpl implements ChargingStationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChargingStationResDto> findByCoordinates(ChargingStationByRangeReqDto reqDto) {
        return queryFactory
                .select(new QChargingStationResDto(
                        chargingStation.name.min(),
                        chargingStation.statId,
                        chargingStation.address.min(),
                        chargingStation.location.min(),
                        chargingStation.latitude.min(),
                        chargingStation.note.min(),
                        chargingStation.limitYn.min(),
                        chargingStation.limitDetail.min(),
                        chargingStation.longitude.min(),
                        chargingStation.useTime.min(),
                        chargingStation.updatedDate.min(),
                        chargingStation.stat.stringValue().min()
                ))
                .from(chargingStation)
                .where(
                        chargingStation.latitude.between(reqDto.getMinLatitude(), reqDto.getMaxLatitude()),
                        chargingStation.longitude.between(reqDto.getMinLongitude(), reqDto.getMaxLongitude()),
                        chargingStation.limitYn.ne("Y"),
                        chargingStation.delYn.ne("Y"),
                        chargingStation.kindDetail.in(KindDetail.getDisplayableCodes()),
                        chargerTypeIn(reqDto.getChargerTypes())
                )
                .groupBy(chargingStation.statId)
                .fetch();
    }

    @Override
    public List<ChargingStationDetailResDto> findDetailByStatId(String statId) {
        return queryFactory
                .select(new QChargingStationDetailResDto(
                        chargingStation.name,
                        chargingStation.statId,
                        chargingStation.chargerId,
                        chargingStation.output,
                        chargingStation.method,
                        chargingStation.kind,
                        chargingStation.kindDetail,
                        chargingStation.address,
                        chargingStation.location,
                        chargingStation.note,
                        chargingStation.limitYn,
                        chargingStation.limitDetail,
                        chargingStation.useTime,
                        chargingStation.updatedDate,
                        chargingStation.stat.stringValue(),
                        chargingStation.chargerType.stringValue()
                ))
                .from(chargingStation)
                .where(chargingStation.statId.eq(statId)
                        .and(chargingStation.limitYn.ne("Y")
                                .and(chargingStation.delYn.ne("Y"))))
                .fetch();
    }

    private BooleanExpression chargerTypeIn(List<String> chargerTypes) {
        if (CollectionUtils.isEmpty(chargerTypes)) {
            return null;
        }
        return chargingStation.chargerType.stringValue().in(chargerTypes);
    }
}
