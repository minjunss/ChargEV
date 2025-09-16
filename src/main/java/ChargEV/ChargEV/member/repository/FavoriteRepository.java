package ChargEV.ChargEV.member.repository;

import ChargEV.ChargEV.member.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByMemberId(Long memberId);
    Optional<Favorite> findByMemberIdAndChargingStationStatId(Long memberId, String chargingStationStatId);
    void deleteByMemberIdAndChargingStationStatId(Long memberId, String chargingStationStatId);
}