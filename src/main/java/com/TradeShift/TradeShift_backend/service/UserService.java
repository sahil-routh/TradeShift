package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.domain.VerificationType;

public interface UserService {
    public User findUseProfileByJwt(String jwt) throws Exception;
    public User findUserByEmail(String email) throws Exception;
    public User findUserById(Long userId) throws Exception;

    public User enableTwoFactorAuthentication(VerificationType verificationType,String sendTo, User user);

    User updatePassword(User user, String newPassword);


}
