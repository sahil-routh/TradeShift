package com.TradeShift.TradeShift_backend.Model;

import com.TradeShift.TradeShift_backend.domain.VerificationType;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class TwoFactorAuth {
    private boolean isEnabled=false;
    private VerificationType sendTo;

}
