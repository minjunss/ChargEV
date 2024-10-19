package com.ChargEV.ChargEV.chargingStation.repository;

import com.ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    List<ChargingStation> findAllByStatIdInAndChargerIdIn(List<String> statIds, List<String> chargerIds);
}
