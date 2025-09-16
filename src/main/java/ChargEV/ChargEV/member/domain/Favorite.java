package ChargEV.ChargEV.member.domain;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.global.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "charging_station_id"})
})
public class Favorite extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_station_id", nullable = false)
    private ChargingStation chargingStation;

    @Builder
    public Favorite(Member member, ChargingStation chargingStation) {
        this.member = member;
        this.chargingStation = chargingStation;
    }
}
