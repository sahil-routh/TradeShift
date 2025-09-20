package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.Model.VerificationCode;
import com.TradeShift.TradeShift_backend.domain.VerificationType;
import com.TradeShift.TradeShift_backend.repository.VerificationCodeRepository;
import com.TradeShift.TradeShift_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;


    @Override
    public VerificationCode sendVerificationCode(User user, VerificationType verificationType) {

        VerificationCode verificationCode1 = new VerificationCode();
        verificationCode1.setOtp(OtpUtils.generateOtp());
        verificationCode1.setVerificationType(verificationType);
        verificationCode1.setUser(user);


        return verificationCodeRepository.save(verificationCode1);
    }

    @Override
    public VerificationCode getVerificationCodeById(Long id) throws Exception {
        Optional<VerificationCode> verificationCode =
                verificationCodeRepository.findById(id);
        if(verificationCode.isPresent()){
            return verificationCode.get();
        }
        throw new Exception("Verification code not found");
    }

    @Override
    public VerificationCode getVerificationCodeByUser(Long userId) {
        return verificationCodeRepository.findByUserId(userId);
    }

    @Override
    public void deleteVerificationCodeById(VerificationCode verificationCode) {

        verificationCodeRepository.delete(verificationCode);
    }
}
