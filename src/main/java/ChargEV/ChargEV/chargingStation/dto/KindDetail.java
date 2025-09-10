package ChargEV.ChargEV.chargingStation.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum KindDetail {
    // 공공/행정
    A001("관공서", true),
    A002("주민센터", true),
    A003("공공기관", true),
    A004("지자체시설", true),

    // 주차장
    B001("공영주차장", true),
    B002("공원주차장", true),
    B003("환승주차장", true),
    B004("일반주차장", true),

    // 휴게소/쉼터
    C001("고속도로 휴게소", true),
    C002("지방도로 휴게소", true),
    C003("쉼터", true),

    // 관광/문화 (공공 개방)
    D001("공원", true),
    D002("전시관", true),
    D003("민속마을", false),
    D004("생태공원", false),
    D005("홍보관", false),
    D006("관광안내소", false),
    D007("관광지", false),
    D008("박물관", false),
    D009("유적지", false),

    // 상업시설
    E001("마트(쇼핑몰)", true),
    E002("백화점", true),
    E003("숙박시설", false),
    E004("골프장(CC)", false),
    E005("카페", true),
    E006("음식점", true),
    E007("주유소", true),
    E008("영화관", false),

    // 정비 관련
    F001("서비스센터", true),
    F002("정비소", true),

    // 군사/폐쇄
    G001("군부대", false),
    G002("야영장", true),
    G003("공중전화부스", false),
    G004("기타", false),
    G005("오피스텔", false),
    G006("단독주택", false),

    // 주거/거주
    H001("아파트", false),
    H002("빌라", false),
    H003("사업장(사옥)", false),
    H004("기숙사", false),
    H005("연립주택", false),

    // 의료/복지/공공
    I001("병원", true),
    I002("종교시설", true),
    I003("보건소", true),
    I004("경찰서", true),
    I005("도서관", true),
    I006("복지관", true),
    I007("수련원", false),
    I008("금융기관", true),

    // 교육/문화
    J001("학교", false),
    J002("교육원", false),
    J003("학원", false),
    J004("공연장", false),
    J005("관람장", false),
    J006("동식물원", false),
    J007("경기장", false);

    private final String description;
    private final boolean displayable;

    KindDetail(String description, boolean displayable) {
        this.description = description;
        this.displayable = displayable;
    }

    public String getCode() {
        return this.name();
    }

    public String getDescription() {
        return description;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public static List<String> getDisplayableCodes() {
        return Arrays.stream(values())
                .filter(KindDetail::isDisplayable)
                .map(KindDetail::getCode)
                .collect(Collectors.toList());
    }
}
