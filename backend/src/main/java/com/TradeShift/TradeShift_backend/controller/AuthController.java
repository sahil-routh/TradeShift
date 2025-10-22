package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.jwt.JwtService;
import com.TradeShift.TradeShift_backend.model.TwoFactorOTP;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.config.JwtProvider;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import com.TradeShift.TradeShift_backend.response.AuthResponse;
import com.TradeShift.TradeShift_backend.service.UserService; // CRITICAL: Need UserService
import com.TradeShift.TradeShift_backend.service.EmailService;
import com.TradeShift.TradeShift_backend.service.TwoFactorOtpService;
import com.TradeShift.TradeShift_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Remove unused dependencies to clean up the code
    // @Autowired
    // private PortfolioService portfolioService;

    // @Autowired
    // private CustomUserDetailsService customUserDetailsService;

    // --- CRITICAL FIX 1: Inject UserService to access the correct registration flow ---
    @Autowired
    private UserService userService;
    // ----------------------------------------------------------------------------------

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        newUser.setFullName(user.getFullName());

        // --- FIX 2: Hash the password and set it on the new User object ---
        String rawPassword = user.getPassword(); // Assuming the incoming user object has the raw password
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        // --- CRITICAL FIX 3: Use the dedicated UserService method ---
        // This method handles saving the user AND calling PortfolioService to create the portfolio.
        User registeredUser = userService.registerUser(newUser);

        // Authenticate the newly registered user by loading their UserDetails
        UserDetails userDetails = (UserDetails) registeredUser;

        // Create an Authentication object using the UserDetails
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                rawPassword, // Use the raw password here for AuthenticationManager to compare
                userDetails.getAuthorities()
        );

        // Set the Authentication in the Security Context
        SecurityContextHolder.getContext().setAuthentication(auth);

        // --- FIX 4: Use the registeredUser object (which is a UserDetails now) to generate JWT ---
        String jwt = jwtService.generateToken((UserDetails) registeredUser);
        // ----------------------------------------------------------------------------------------

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("Register Success");
        // Add user details to response for frontend
        res.setUserId(registeredUser.getId());
        res.setEmail(registeredUser.getEmail());
        res.setFullName(registeredUser.getFullName());


        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) throws Exception {

        String userName = user.getEmail();
        String password = user.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // The authentication.getPrincipal() returns UserDetails, which is required by jwtService
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
        // Add user details to response
        if (authuser != null) {
            res.setUserId(authuser.getId());
            res.setEmail(authuser.getEmail());
            res.setFullName(authuser.getFullName());
        }

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

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