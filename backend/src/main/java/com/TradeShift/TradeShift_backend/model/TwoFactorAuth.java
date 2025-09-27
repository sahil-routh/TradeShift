package com.TradeShift.TradeShift_backend.model;

import com.TradeShift.TradeShift_backend.domain.VerificationType;
import lombok.Data;

@Data
public class TwoFactorAuth {
    private boolean isEnabled=false;
    private VerificationType sendTo;

}
