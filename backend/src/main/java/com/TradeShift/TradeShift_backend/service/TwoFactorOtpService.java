package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.TwoFactorOTP;
import com.TradeShift.TradeShift_backend.Model.User;

public interface TwoFactorOtpService {

    TwoFactorOTP createTwoFactorOTP(User user, String otp, String jwt);

    TwoFactorOTP findByUser(long userId);

    TwoFactorOTP findById(String id);

    boolean verifyTwoFactorOtp(TwoFactorOTP twoFactorOTP,String otp);

    void deleteTwoFactorOtp(TwoFactorOTP twoFactorOTP);

}
