package com.TradeShift.TradeShift_backend.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String otp;
    private String password;

}
