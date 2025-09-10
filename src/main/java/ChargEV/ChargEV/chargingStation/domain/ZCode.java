package ChargEV.ChargEV.chargingStation.domain;

public enum ZCode {
    SEOUL(11, "서울특별시"),
    BUSAN(26, "부산광역시"),
    DAEGU(27, "대구광역시"),
    INCHEON(28, "인천광역시"),
    GWANGJU(29, "광주광역시"),
    DAEJEON(30, "대전광역시"),
    ULSAN(31, "울산광역시"),
    SEJONG(36, "세종특별자치시"),
    GYEONGGI(41, "경기도"),
    CHUNGBUK(43, "충청북도"),
    CHUNGNAM(44, "충청남도"),
    JEONNAM(46, "전라남도"),
    GYEONGBUK(47, "경상북도"),
    GYEONGNAM(48, "경상남도"),
    JEJU(50, "제주특별자치도"),
    GANGWON(51, "강원특별자치도"),
    JEONBUK(52, "전북특별자치도");

    private final int code;
    private final String region;

    ZCode(int code, String region) {
        this.code = code;
        this.region = region;
    }

    public int getCode() {
        return code;
    }

    public String getRegion() {
        return region;
    }

    public static ZCode fromCode(int code) {
        for (ZCode z : ZCode.values()) {
            if (z.code == code) {
                return z;
            }
        }
        throw new IllegalArgumentException("Unknown ZCode: " + code);
    }
}
