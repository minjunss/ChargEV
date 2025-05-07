package com.ChargEV.ChargEV.chargingStation.repository;

import com.ChargEV.ChargEV.chargingStation.dto.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.ChargEV.ChargEV.chargingStation.domain.QChargingStation.chargingStation;

@RequiredArgsConstructor
public class ChargingStationRepositoryCustomImpl implements ChargingStationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ChargingStationResDto> findByCoordinates(ChargingStationByRangeReqDto reqDto) {
        return jpaQueryFactory
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
                        chargingStation.updatedDate.min()
                ))
                .from(chargingStation)
                .where(chargingStation.latitude.between(reqDto.getMinLatitude(), reqDto.getMaxLatitude())
                        .and(chargingStation.longitude.between(reqDto.getMinLongitude(), reqDto.getMaxLongitude()))
                        .and(chargingStation.limitYn.ne("Y"))
                        .and(chargingStation.delYn.ne("Y")))
                .groupBy(chargingStation.statId)
                .fetch();
    }

    @Override
    public List<ChargingStationDetailResDto> findByDetailByStatId(String statId) {
        return jpaQueryFactory
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
                        chargingStation.updatedDate
                ))
                .from(chargingStation)
                .where(chargingStation.statId.eq(statId)
                        .and(chargingStation.limitYn.ne("Y")
                        .and(chargingStation.delYn.ne("Y"))))
                .fetch();
    }
}
