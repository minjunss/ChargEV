package ChargEV.ChargEV.chargingStation.domain;

public enum ChargerType {
    DC_CHADEMO("01", "DC차데모"),
    AC_SLOW("02", "AC완속"),
    DC_CHADEMO_AC_TRIPLE("03", "DC차데모+AC3상"),
    DC_COMBO("04", "DC콤보"),
    DC_CHADEMO_COMBO("05", "DC차데모+DC콤보"),
    DC_CHADEMO_AC_TRIPLE_COMBO("06", "DC차데모+AC3상+DC콤보"),
    AC_TRIPLE("07", "AC3상"),
    DC_COMBO_SLOW("08", "DC콤보(완속)"),
    NACS("09", "NACS"),
    DC_COMBO_NACS("10", "DC콤보+NACS");

    private final String code;
    private final String description;

    ChargerType(String code, String description) {
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
        for (ChargerType type : ChargerType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "Unknown";
    }

    public static ChargerType fromCode(String code) {
        for(ChargerType type : ChargerType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ChargerType code: " + code);
    }
}
