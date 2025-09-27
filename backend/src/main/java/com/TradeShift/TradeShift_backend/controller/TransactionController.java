package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.Transaction;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.response.ApiResponse;
import com.TradeShift.TradeShift_backend.service.PortfolioService;
import com.TradeShift.TradeShift_backend.service.TransactionService;
import com.TradeShift.TradeShift_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private UserService userService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TransactionService transactionService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String email = auth.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getTransactionHistory() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication failed."), HttpStatus.UNAUTHORIZED);
        }
        try {
            Portfolio portfolio = portfolioService.getPortfolioForUser(currentUser);
            List<Transaction> transactions = transactionService.getTransactionsForPortfolio(portfolio);
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}