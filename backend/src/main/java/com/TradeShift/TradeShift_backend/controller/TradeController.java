package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.request.TradeRequest;
import com.TradeShift.TradeShift_backend.response.ApiResponse;
import com.TradeShift.TradeShift_backend.service.AssetService;
import com.TradeShift.TradeShift_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio/trade")
public class TradeController {

    @Autowired
    private UserService userService;

    @Autowired
    private AssetService assetService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        String email = auth.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    // Handles trade requests for both BUY and SELL
    private ResponseEntity<ApiResponse> executeTrade(String type, TradeRequest req) {
        User user = getCurrentUser();
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication required."), HttpStatus.UNAUTHORIZED);
        }
        if (req.getSymbol() == null || req.getSymbol().isEmpty() || req.getQuantity() <= 0) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid symbol or quantity."), HttpStatus.BAD_REQUEST);
        }

        try {
            Portfolio portfolio = user.getPortfolio(); // Assuming User entity has getPortfolio()
            String symbol = req.getSymbol().toUpperCase();
            int quantity = req.getQuantity();

            Asset result;
            if ("BUY".equalsIgnoreCase(type)) {
                // NOTE: We need the asset name for a BUY trade.
                // For now, we'll use the symbol as the name, or fetch it (next step).
                result = assetService.buyAsset(portfolio, symbol, symbol, quantity);
                return new ResponseEntity<>(new ApiResponse(true,
                        String.format("Successfully bought %d shares of %s at market price.", quantity, symbol)),
                        HttpStatus.OK);
            } else if ("SELL".equalsIgnoreCase(type)) {
                result = assetService.sellAsset(portfolio, symbol, quantity);
                return new ResponseEntity<>(new ApiResponse(true,
                        String.format("Successfully sold %d shares of %s at market price.", quantity, symbol)),
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse(false, "Invalid trade type."), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse> buy(@RequestBody TradeRequest req) {
        return executeTrade("BUY", req);
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse> sell(@RequestBody TradeRequest req) {
        return executeTrade("SELL", req);
    }
}