package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.Model.VerificationCode;
import com.TradeShift.TradeShift_backend.domain.VerificationType;

public interface VerificationCodeService {
    VerificationCode sendVerificationCode(User user, VerificationType verificationType);

    VerificationCode getVerificationCodeById(Long id) throws Exception;

    VerificationCode getVerificationCodeByUser(Long userId);


    void deleteVerificationCodeById(VerificationCode verificationCode);

}
