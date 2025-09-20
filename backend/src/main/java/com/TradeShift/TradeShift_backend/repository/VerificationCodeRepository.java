package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.Model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    public VerificationCode findByUserId(Long userId);
}
