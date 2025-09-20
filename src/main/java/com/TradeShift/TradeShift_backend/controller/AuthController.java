package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.Model.TwoFactorOTP;
import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.config.JwtProvider;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import com.TradeShift.TradeShift_backend.response.AuthResponse;
import com.TradeShift.TradeShift_backend.service.CustomUserDetailsService;
import com.TradeShift.TradeShift_backend.service.EmailService;
import com.TradeShift.TradeShift_backend.service.TwoFactorOtpService;
import com.TradeShift.TradeShift_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private TwoFactorOtpService twoFactorOtpService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {


        User isEmailExist=userRepository.findByEmail(user.getEmail());
        if(isEmailExist!=null){
            throw new Exception("Email is already used with another account");
        }

        User newUser=new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setEmail(user.getEmail());
        newUser.setFullName(user.getFullName());


        User savedUser=userRepository.save(newUser);

        Authentication auth=new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt= JwtProvider.generateToken(auth);

        AuthResponse res=new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("Register Success");





        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) throws Exception {

        String userName = user.getEmail();
        String password = user.getPassword();

        Authentication auth= authenticate(userName,password);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt= JwtProvider.generateToken(auth);

        User authuser = userRepository.findByEmail(userName);

        if(user.getTwoFactorAuth().isEnabled()){
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enable");
            res.setTwoFactorAuthEnabled(true);
            String otp = OtpUtils.generateOtp();

            TwoFactorOTP oldTwoFactorOTP = twoFactorOtpService.findByUser(authuser.getId());
            if(oldTwoFactorOTP!=null){
                twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOTP);
            }

            TwoFactorOTP newTwoFactorOTP = twoFactorOtpService.createTwoFactorOTP(authuser, otp, jwt);


            emailService.sendVerificationOtpEmail(userName,otp);


            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res,HttpStatus.ACCEPTED);
        }

        AuthResponse res=new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("login Success");

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);

        if(userDetails == null){
            throw new BadCredentialsException("invalid user");
        }
        if(!password.equals(userDetails.getPassword())){
            throw new BadCredentialsException("invalid password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails,password,userDetails.getAuthorities());
    }
    @PostMapping("/two-factor/otp/{otp}")
public ResponseEntity<AuthResponse> verifySignInOtp(
        @PathVariable String otp,
        @RequestParam String id) throws Exception {

        TwoFactorOTP twoFactorOTP = twoFactorOtpService.findById(id);

        if(twoFactorOtpService.verifyTwoFactorOtp(twoFactorOTP,otp)){
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor authentication verified");
            res.setTwoFactorAuthEnabled(true);
            res.setJwt(twoFactorOTP.getJwt());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }

}
