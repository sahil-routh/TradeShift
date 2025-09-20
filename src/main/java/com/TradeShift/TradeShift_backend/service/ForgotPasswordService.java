package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.ForgotPasswordToken;
import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.domain.VerificationType;

public interface ForgotPasswordService {

    ForgotPasswordToken createToken(User user,
                                    String id, String otp,
                                    VerificationType verificationType,
                                    String sendTo);

    ForgotPasswordToken findById(String id);

    ForgotPasswordToken findByUser(Long userId);

    void deleteToken(ForgotPasswordToken token);
}
