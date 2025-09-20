package com.TradeShift.TradeShift_backend.response;

import lombok.Data;

@Data
public class AuthResponse {

    private String jwt;
    private  boolean status;
    private  String message;
    private boolean isTwoFactorAuthEnabled;
    private String session;
}
