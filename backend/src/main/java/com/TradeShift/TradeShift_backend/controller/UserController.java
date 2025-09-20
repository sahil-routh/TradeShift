package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.request.ForgotPasswordTokenRequest;
import com.TradeShift.TradeShift_backend.Model.ForgotPasswordToken;
import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.Model.VerificationCode;
import com.TradeShift.TradeShift_backend.domain.VerificationType;

import com.TradeShift.TradeShift_backend.request.ResetPasswordRequest;
import com.TradeShift.TradeShift_backend.response.ApiResponse;
import com.TradeShift.TradeShift_backend.response.AuthResponse;
import com.TradeShift.TradeShift_backend.service.EmailService;
import com.TradeShift.TradeShift_backend.service.ForgotPasswordService;
import com.TradeShift.TradeShift_backend.service.UserService;
import com.TradeShift.TradeShift_backend.service.VerificationCodeServiceImpl;
import com.TradeShift.TradeShift_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeServiceImpl verificationCodeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;
    private String jwt;

    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt ) throws Exception {
        User user = userService.findUseProfileByJwt(jwt);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @PostMapping("/api/users/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOtp(
            @RequestHeader("Authorization") String jwt,
            @PathVariable VerificationType
            verificationType) throws Exception {
        User user = userService.findUseProfileByJwt(jwt);

        VerificationCode verificationCode = verificationCodeService.
                getVerificationCodeByUser(user.getId());

        if (verificationCode == null) {
            verificationCode = verificationCodeService.
                    sendVerificationCode(user, verificationType);
        }
        if(verificationType.equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
        }
            return new ResponseEntity<>("OTP successfully send", HttpStatus.OK);
        }

        @PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
        public ResponseEntity<User> enableTwoFactorAuthentication (
                @RequestHeader("Authorization") String jwt,
                @PathVariable String otp,
                @PathVariable VerificationType verificationType ) throws Exception {
            User user = userService.findUseProfileByJwt(jwt);

            VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
            String sendTO= verificationCode.getVerificationType().equals(VerificationType.EMAIL)?
                    verificationCode.getEmail():verificationCode.getMobile();

            boolean isVerified = verificationCode.getOtp().equals(otp);
            if(isVerified){
                User updatedUser = userService.enableTwoFactorAuthentication(
                        verificationCode.getVerificationType(),sendTO, user);
                verificationCodeService.deleteVerificationCodeById(verificationCode);
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            }
           throw new Exception("Wrong OTP");
        }


    @PostMapping("/auth/users/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgotPasswordOtp(

            @RequestBody ForgotPasswordTokenRequest req) throws Exception {


        User user =userService.findUserByEmail(req.getSendTo());
        String otp = OtpUtils.generateOtp();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        ForgotPasswordToken token = forgotPasswordService.findByUser(user.getId());
        if(token == null){
            token=forgotPasswordService.createToken(user, id,otp,req.getVerificationType(), req.getSendTo());
        }

        if(req.getVerificationType().equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtpEmail(
                    user.getEmail(),
                    token.getOtp());
        }
        AuthResponse response = new AuthResponse();
        response.setSession(token.getId());
        response.setMessage("Password reset otp sent successfully");


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/auth/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse> resetPassword (
            @RequestParam String id,
            @RequestBody ResetPasswordRequest req,
            @RequestHeader("Authorization") String jwt,
            @PathVariable VerificationType verificationType ) throws Exception {


        ForgotPasswordToken forgotPasswordToken = forgotPasswordService.findById(id);
        boolean isVerified = forgotPasswordToken.getOtp().equals(req.getOtp());
        if(isVerified){

            userService.updatePassword(forgotPasswordToken.getUser(),req.getPassword());
            ApiResponse res = new ApiResponse();
            res.setMessage("Password updated successfully");
            return new ResponseEntity<>(res,HttpStatus.ACCEPTED);
        }
        throw new Exception("Wrong OTP");



    }



}
