package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.response.ApiResponse; // Using a structured response
import com.TradeShift.TradeShift_backend.request.BuyAssetRequest;
import com.TradeShift.TradeShift_backend.request.SellAssetRequest;
import com.TradeShift.TradeShift_backend.service.AssetService;
import com.TradeShift.TradeShift_backend.service.PortfolioService;
import com.TradeShift.TradeShift_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserService userService;

    @Autowired
    private PortfolioService portfolioService;

    // A helper method to get the authenticated user.
    // This is now safer and cleaner than the old method.
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

    // Endpoint for buying an asset
    @PostMapping("/buy")
    public ResponseEntity<?> buyAsset(@RequestBody BuyAssetRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse("Authentication failed."), HttpStatus.UNAUTHORIZED);
        }
        try {
            Portfolio portfolio = portfolioService.getPortfolioForUser(currentUser);
            Asset purchasedAsset = assetService.buyAsset(
                    portfolio,
                    request.getSymbol(),
                    request.getName(),
                    request.getQuantity()
            );
            return new ResponseEntity<>(purchasedAsset, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint for selling an asset
    @PostMapping("/sell")
    public ResponseEntity<?> sellAsset(@RequestBody SellAssetRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse("Authentication failed."), HttpStatus.UNAUTHORIZED);
        }
        try {
            Portfolio portfolio = portfolioService.getPortfolioForUser(currentUser);
            Asset soldAsset = assetService.sellAsset(
                    portfolio,
                    request.getSymbol(),
                    request.getQuantity()
            );
            return new ResponseEntity<>(soldAsset, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to get all assets for the authenticated user's portfolio
    @GetMapping("/my-assets")
    public ResponseEntity<?> getMyAssets() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse("Authentication failed."), HttpStatus.UNAUTHORIZED);
        }
        try {
            Portfolio portfolio = portfolioService.getPortfolioForUser(currentUser);
            List<Asset> assets = assetService.getAssetsForPortfolio(portfolio);
            return new ResponseEntity<>(assets, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}