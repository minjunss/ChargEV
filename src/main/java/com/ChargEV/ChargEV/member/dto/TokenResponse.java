package com.ChargEV.ChargEV.member.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String access_token;
    private String id_token;
    private String scope;
}
