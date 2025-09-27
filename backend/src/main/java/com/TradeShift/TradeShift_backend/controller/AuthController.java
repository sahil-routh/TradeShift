package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.jwt.JwtService;
import com.TradeShift.TradeShift_backend.model.TwoFactorOTP;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.config.JwtProvider;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import com.TradeShift.TradeShift_backend.response.AuthResponse;
import com.TradeShift.TradeShift_backend.service.PortfolioService;
import com.TradeShift.TradeShift_backend.service.CustomUserDetailsService;
import com.TradeShift.TradeShift_backend.service.EmailService;
import com.TradeShift.TradeShift_backend.service.TwoFactorOtpService;
import com.TradeShift.TradeShift_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// IMPORTANT: Add these three imports for Spring Authentication
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
// -----------------------------------------------------------
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
    private PortfolioService portfolioService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // -----------------------------------------------------------
    // Inject the AuthenticationManager bean from SecurityConfig
    @Autowired
    private AuthenticationManager authenticationManager;

    // Inject PasswordEncoder to hash passwords on signup
    @Autowired
    private PasswordEncoder passwordEncoder;
    // -----------------------------------------------------------

    @Autowired
    private TwoFactorOtpService twoFactorOtpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {

        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if (isEmailExist != null) {
            throw new Exception("Email is already used with another account");
        }

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        // -----------------------------------------------------------
        // FIX 1: Hash the password before saving to the database
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        newUser.setPassword(hashedPassword);
        // -----------------------------------------------------------
        newUser.setFullName(user.getFullName());

        User savedUser = userRepository.save(newUser);

        portfolioService.createPortfolioForUser(savedUser);

        // Authenticate the newly registered user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword()
        );
        // Note: Spring will successfully authenticate here because the password encoder is used
        // throughout the process.

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtProvider.generateToken(auth);

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("Register Success");

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) throws Exception {

        String userName = user.getEmail();
        String password = user.getPassword();

        // -----------------------------------------------------------
        // FIX 2: Delegate authentication to Spring's AuthenticationManager.
        // This manager uses the AuthenticationProvider (defined in SecurityConfig)
        // to call the CustomUserDetailsService and use the BCryptPasswordEncoder
        // to compare the password hash.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, password)
        );
        // -----------------------------------------------------------

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtService.generateToken((UserDetails) authentication.getPrincipal());


        User authuser = userRepository.findByEmail(userName);

        // Use 'authuser' which has been loaded from the database
        if (authuser != null && authuser.getTwoFactorAuth() != null && authuser.getTwoFactorAuth().isEnabled()) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enable");
            res.setTwoFactorAuthEnabled(true);

            String otp = OtpUtils.generateOtp();

            TwoFactorOTP oldTwoFactorOTP = twoFactorOtpService.findByUser(authuser.getId());
            if (oldTwoFactorOTP != null) {
                twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOTP);
            }

            TwoFactorOTP newTwoFactorOTP = twoFactorOtpService.createTwoFactorOTP(authuser, otp, jwt);

            emailService.sendVerificationOtpEmail(userName, otp);

            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("login Success");

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    // -----------------------------------------------------------
    // FIX 3: DELETE this private authenticate method. It is no longer needed
    // because you are using the Spring AuthenticationManager.
    /*
    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);

        if (userDetails == null) {
            throw new BadCredentialsException("invalid user");
        }
        if (!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }
    */
    // -----------------------------------------------------------

    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<AuthResponse> verifySignInOtp(
            @PathVariable String otp,
            @RequestParam String id) throws Exception {

        TwoFactorOTP twoFactorOTP = twoFactorOtpService.findById(id);

        if (twoFactorOtpService.verifyTwoFactorOtp(twoFactorOTP, otp)) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor authentication verified");
            res.setTwoFactorAuthEnabled(true);
            res.setJwt(twoFactorOTP.getJwt());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }

}