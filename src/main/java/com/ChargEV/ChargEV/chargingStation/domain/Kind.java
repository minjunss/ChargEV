package com.ChargEV.ChargEV.chargingStation.domain;

public enum Kind {
    A0("공공시설"),
    B0("주차시설"),
    C0("휴게시설"),
    D0("관광시설"),
    E0("상업시설"),
    F0("차량정비시설"),
    G0("기타시설"),
    H0("공동주택시설"),
    I0("근린생활시설"),
    J0("교육문화시설");

    private final String description;

    Kind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
