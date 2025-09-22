package com.TradeShift.TradeShift_backend.request;

import com.TradeShift.TradeShift_backend.domain.VerificationType;
import lombok.Data;

@Data
public class ForgotPasswordTokenRequest {
    private String sendTo;
    private VerificationType verificationType;
}
