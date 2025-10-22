package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.TwoFactorAuth;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.model.Portfolio; //changed
import com.TradeShift.TradeShift_backend.config.JwtProvider;
import com.TradeShift.TradeShift_backend.domain.VerificationType;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioService portfolioService;

    // --- NEW METHOD STRUCTURE: Example for handling new user registration ---
    // You will need to call this from your AuthController after hashing the password.
    public User registerUser(User user) {

        // Save the user first (which also sets the ID)
        User savedUser = userRepository.save(user);

        // --- CRITICAL FIX 3: Initialize Portfolio and link it ---
        Portfolio portfolio = new Portfolio();
        portfolio.setUser(savedUser); // Link the portfolio back to the new user
        portfolio.setCashBalance(10000.00); // Initialize with a starting cash balance
        portfolio.setTotalValue(10000.00); // Initial value = initial cash

        // Save the linked portfolio
        portfolioService.createPortfolio(portfolio);

        return savedUser;
    }


    //--------
    @Override
    public User findUseProfileByJwt(String jwt) throws Exception {
        String email = JwtProvider.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new Exception("User not found");
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new Exception("User not found");
        }
        return user;
    }

    @Override
    public User findUserById(Long userId) throws Exception {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user) {
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setSendTo(verificationType);

        user.setTwoFactorAuth(twoFactorAuth);


        return userRepository.save(user);
    }


    @Override
    public User updatePassword(User user, String newPassword) {

        user.setPassword(newPassword);
        return userRepository.save(user);
    }
}
