package ChargEV.ChargEV.chargingStation.repository;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long>, ChargingStationRepositoryCustom {
    List<ChargingStation> findByStatId(String statId);
}
