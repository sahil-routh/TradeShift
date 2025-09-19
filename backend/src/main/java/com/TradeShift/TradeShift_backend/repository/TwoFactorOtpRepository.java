package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.Model.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorOtpRepository extends JpaRepository<TwoFactorOTP,String> {

    TwoFactorOTP findByUserId(Long UserId);
}
