package ChargEV.ChargEV.chargingStation.dto;

import ChargEV.ChargEV.chargingStation.domain.ChargerType;
import ChargEV.ChargEV.chargingStation.domain.Stat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ChargingStationDetailResDto {
    private String name; //이름
    private String statId; //충전소 ID
    private String chargerId; //충전기 ID
    private String output; //충전용량
    private String method; //충전방식
    private String kind; //충전소 구분 코드
    private String kindDetail; //충전소 구분 상세코드
    private String address; //주소
    private String location; //상세위치
    private String note; //안내
    private String limitYn; //이용자 제한
    private String limitDetail; //이용제한 사유
    private String useTime; //이용가능시간
    private String updatedDate; //상태갱신일시
    private String stat; // 충전기상태
    private String chargerType; // 충전기타입

    @QueryProjection
    public ChargingStationDetailResDto(String name, String statId, String chargerId, String output, String method, String kind, String kindDetail, String address, String location, String note, String limitYn, String limitDetail, String useTime, String updatedDate, String stat, String chargerType) {
        this.name = name;
        this.statId = statId;
        this.chargerId = chargerId;
        this.output = output;
        this.method = method;
        this.kind = kind;
        this.kindDetail = kindDetail;
        this.address = address;
        this.location = location;
        this.note = note;
        this.limitYn = limitYn;
        this.limitDetail = limitDetail;
        this.useTime = useTime;
        this.updatedDate = updatedDate;
        this.stat = Stat.getDescriptionByCode(stat);
        this.chargerType = ChargerType.getDescriptionByCode(chargerType);
    }
}