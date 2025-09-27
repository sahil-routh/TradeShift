package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.model.VerificationCode;
import com.TradeShift.TradeShift_backend.domain.VerificationType;

public interface VerificationCodeService {
    VerificationCode sendVerificationCode(User user, VerificationType verificationType);

    VerificationCode getVerificationCodeById(Long id) throws Exception;

    VerificationCode getVerificationCodeByUser(Long userId);


    void deleteVerificationCodeById(VerificationCode verificationCode);

}
