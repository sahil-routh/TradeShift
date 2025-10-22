package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.model.Watchlist;
import com.TradeShift.TradeShift_backend.repository.WatchlistRepository;
import com.TradeShift.TradeShift_backend.response.ApiResponse;
import com.TradeShift.TradeShift_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "http://localhost:5173")
public class WatchlistController {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private UserService userService;

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

    /**
     * Get all watchlist items for the authenticated user
     * GET /api/watchlist
     */
    @GetMapping
    public ResponseEntity<?> getWatchlist() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication failed."), HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Watchlist> watchlist = watchlistRepository.findByUserOrderByAddedAtDesc(currentUser);

            // Return simplified data (just symbols and names)
            List<Map<String, Object>> response = watchlist.stream().map(w -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", w.getId());
                item.put("symbol", w.getSymbol());
                item.put("name", w.getName());
                item.put("addedAt", w.getAddedAt());
                return item;
            }).collect(Collectors.toList());

            System.out.println("✅ Watchlist fetched for user: " + currentUser.getEmail() + " (" + response.size() + " items)");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching watchlist: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add a stock to watchlist
     * POST /api/watchlist
     * Body: { "symbol": "AAPL", "name": "Apple Inc." }
     */
    @PostMapping
    public ResponseEntity<?> addToWatchlist(@RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication failed."), HttpStatus.UNAUTHORIZED);
        }

        String symbol = request.get("symbol");
        String name = request.get("name");

        if (symbol == null || symbol.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Symbol is required."), HttpStatus.BAD_REQUEST);
        }

        symbol = symbol.toUpperCase();

        try {
            // Check if already in watchlist
            if (watchlistRepository.existsByUserAndSymbol(currentUser, symbol)) {
                return new ResponseEntity<>(new ApiResponse(false, symbol + " is already in your watchlist."), HttpStatus.CONFLICT);
            }

            Watchlist watchlistItem = new Watchlist();
            watchlistItem.setUser(currentUser);
            watchlistItem.setSymbol(symbol);
            watchlistItem.setName(name != null ? name : symbol);

            Watchlist saved = watchlistRepository.save(watchlistItem);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("symbol", saved.getSymbol());
            response.put("name", saved.getName());
            response.put("addedAt", saved.getAddedAt());
            response.put("message", symbol + " added to watchlist");

            System.out.println("✅ Added to watchlist: " + symbol + " for user: " + currentUser.getEmail());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("❌ Error adding to watchlist: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse(false, "Error adding to watchlist: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a stock from watchlist
     * DELETE /api/watchlist/{symbol}
     */
    @DeleteMapping("/{symbol}")
    @Transactional
    public ResponseEntity<?> removeFromWatchlist(@PathVariable String symbol) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication failed."), HttpStatus.UNAUTHORIZED);
        }

        symbol = symbol.toUpperCase();

        try {
            if (!watchlistRepository.existsByUserAndSymbol(currentUser, symbol)) {
                return new ResponseEntity<>(new ApiResponse(false, symbol + " is not in your watchlist."), HttpStatus.NOT_FOUND);
            }

            watchlistRepository.deleteByUserAndSymbol(currentUser, symbol);

            System.out.println("✅ Removed from watchlist: " + symbol + " for user: " + currentUser.getEmail());
            return ResponseEntity.ok(new ApiResponse(true, symbol + " removed from watchlist"));
        } catch (Exception e) {
            System.err.println("❌ Error removing from watchlist: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse(false, "Error removing from watchlist: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if a stock is in watchlist
     * GET /api/watchlist/check/{symbol}
     */
    @GetMapping("/check/{symbol}")
    public ResponseEntity<?> checkWatchlist(@PathVariable String symbol) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication failed."), HttpStatus.UNAUTHORIZED);
        }

        symbol = symbol.toUpperCase();

        try {
            boolean exists = watchlistRepository.existsByUserAndSymbol(currentUser, symbol);

            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("inWatchlist", exists);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}