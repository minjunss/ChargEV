package ChargEV.ChargEV.chargingStation.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingStationResDto {
    private String name; //이름
    private String statId; //충전소 ID
    private String address; //주소
    private String location; //상세위치
    private Double latitude; //위도
    private Double longitude; //경도
    private String note; //안내
    private String limitYn; //이용자 제한
    private String limitDetail; //이용제한 사유
    private String useTime; //이용가능시간
    private String updatedDate; //상태갱신일시
    private String stat; //충전기 상태
    private boolean hasAvailableCharger; // 충전 가능한 충전기가 있는지 여부

    @QueryProjection
    @Builder
    public ChargingStationResDto(String name, String statId, String address, String location, Double latitude, String note, String limitYn, String limitDetail, Double longitude, String useTime, String updatedDate, String stat, boolean hasAvailableCharger) {
        this.name = name;
        this.statId = statId;
        this.address = address;
        this.location = location;
        this.latitude = latitude;
        this.note = note;
        this.limitYn = limitYn;
        this.limitDetail = limitDetail;
        this.longitude = longitude;
        this.useTime = useTime;
        this.updatedDate = updatedDate;
        this.stat = stat;
        this.hasAvailableCharger = hasAvailableCharger;
    }
}
