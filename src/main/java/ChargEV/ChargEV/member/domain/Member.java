package ChargEV.ChargEV.member.domain;

import ChargEV.ChargEV.global.BaseTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String nickName;
    private String carNum;
    private Double latitude;
    private Double longitude;

    @Builder
    public Member(String email, String nickName, String carNum, Double latitude, Double longitude) {
        this.email = email;
        this.nickName = nickName;
        this.carNum = carNum;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
