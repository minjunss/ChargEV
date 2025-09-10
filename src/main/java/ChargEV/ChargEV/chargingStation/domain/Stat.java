package ChargEV.ChargEV.chargingStation.domain;

public enum Stat {
    COMMUNICATION_ERROR("1", "통신이상"),
    WAITING("2", "충전대기"),
    CHARGING("3", "충전중"),
    STOPPED("4", "운영중지"),
    INSPECTION("5", "점검중"),
    UNKNOWN("7", "상태미확인"),
    UNKNOWN2("9", "상태미확인"),
    UNKNOWN3("0", "상태미확인"),
    UNKNOWN4("6", "상태미확인");

    private final String code;
    private final String description;

    Stat(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescriptionByCode(String code) {
        for (Stat status : Stat.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "Unknown";
    }

    public static Stat fromCode(String code) {
        for (Stat status : Stat.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown Stat code: " + code);
    }
}
