package ChargEV.ChargEV.chargingStation.domain;

import ChargEV.ChargEV.chargingStation.converter.ChargerTypeConverter;
import ChargEV.ChargEV.chargingStation.converter.ZCodeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "charging_station", indexes = {
        @Index(name = "idx_statId", columnList = "statId"),
        @Index(name = "idx_chargerId", columnList = "chargerId"),
})
@Getter
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name; //이름
    private String statId; //충전소 ID
    private String chargerId; //충전기 ID
    @Convert(converter = ChargerTypeConverter.class)
    private ChargerType chargerType; //충전기타입
    private String address; //주소
    private String location; //상세위치
    private Double latitude; //위도
    private String note; //안내
    private String limitYn; //이용자 제한
    private String limitDetail; //이용제한 사유
    private Double longitude; //경도
    private String useTime; //이용가능시간
    private Stat stat; //충전기상태
    private String output; //충전용량
    private String method; //충전방식
    private String kind; //충전소 구분 코드
    private String kindDetail; //충전소 구분 상세코드
    @Convert(converter = ZCodeConverter.class)
    private ZCode zcode; //지역코드
    private String updatedDate; //상태갱신일시
    private String delYn; //삭제 여부

    @Builder
    public ChargingStation(String name, String statId, String chargerId, ChargerType chargerType, String address, String location, Double latitude, String note, String limitYn, String limitDetail, Double longitude, String useTime, Stat stat, String output, String method, String kind, String kindDetail, ZCode zcode, String updatedDate, String delYn) {
        this.name = name;
        this.statId = statId;
        this.chargerId = chargerId;
        this.chargerType = chargerType;
        this.address = address;
        this.location = location;
        this.latitude = latitude;
        this.note = note;
        this.limitYn = limitYn;
        this.limitDetail = limitDetail;
        this.longitude = longitude;
        this.useTime = useTime;
        this.stat = stat;
        this.output = output;
        this.method = method;
        this.kind = kind;
        this.kindDetail = kindDetail;
        this.zcode = zcode;
        this.updatedDate = updatedDate;
        this.delYn = delYn;
    }

    public void update(String name, String statId, String chargerId, ChargerType chargerType, String address, String location, Double latitude, String note, String limitYn, String limitDetail, Double longitude, String useTime, Stat stat, String output, String method, String kind, String kindDetail, ZCode zcode, String updatedDate, String delYn) {
        this.name = name;
        this.statId = statId;
        this.chargerId = chargerId;
        this.chargerType = chargerType;
        this.address = address;
        this.location = location;
        this.latitude = latitude;
        this.note = note;
        this.limitYn = limitYn;
        this.limitDetail = limitDetail;
        this.longitude = longitude;
        this.useTime = useTime;
        this.stat = stat;
        this.output = output;
        this.method = method;
        this.kind = kind;
        this.kindDetail = kindDetail;
        this.zcode = zcode;
        this.updatedDate = updatedDate;
        this.delYn = delYn;
    }

}
