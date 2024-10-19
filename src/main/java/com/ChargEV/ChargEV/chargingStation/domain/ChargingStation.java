package com.ChargEV.ChargEV.chargingStation.domain;

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
    /*
    충전기타입
    01: DC차데모,
    02: AC완속,
    03: DC차데모+AC3상,
    04: DC콤보,
    05: DC차데모+DC콤보
    06: DC차데모+AC3상+DC콤보,
    07: AC3상
    08: DC콤보(완속)
    */
    private String chargerType;
    private String address; //주소
    //상세위치
    private String location;
    private Double latitude; //위도
    private String note; //안내
    private String limitYn; //이용자 제한
    private String limitDetail; //이용제한 사유
    private Double longitude; //경도
    private String useTime; //이용가능시간
    /*
    충전기상태
    1: 통신이상,
    2: 충전대기,
    3: 충전중,
    4: 운영중지,
    5: 점검중,
    9: 상태미확인
    */
    private String stat;
    private String output; //충전용량
    private String method; //충전방식
    private String kind; //충전소 구분 코드
    private String kindDetail; //충전소 구분 상세코드
    /*
    지역코드
    11: 서울특별시
    26: 부산광역시
    27: 대구광역시
    28: 인천광역시
    29: 광주광역시
    30: 대전광역시
    31: 울산광역시
    36: 세종특별자치시
    41: 경기도
    43: 충청북도
    44: 충청남도
    46: 전라남도
    47: 경상북도
    48: 경상남도
    50: 제주특별자치도
    51: 강원특별자치도
    52: 전북특별자치도
    */
    private String zcode;
    private String updatedDate; //상태갱신일시
    private String delYn; //삭제 여부

    @Builder
    public ChargingStation(String name, String statId, String chargerId, String chargerType, String address, String location, Double latitude, String note, String limitYn, String limitDetail, Double longitude, String useTime, String stat, String output, String method, String kind, String kindDetail, String zcode, String updatedDate, String delYn) {
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


    public void update(String name, String statId, String chargerId, String chargerType, String address, String location, Double latitude, String note, String limitYn, String limitDetail, Double longitude, String useTime, String stat, String output, String method, String kind, String kindDetail, String zcode, String updatedDate, String delYn) {
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
