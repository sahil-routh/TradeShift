package com.TradeShift.TradeShift_backend.request;

import lombok.Data;

/**
 * DTO for handling incoming trade requests from the frontend.
 * This structure corresponds to the JSON sent via the /api/trade POST request.
 */
@Data
public class TradeRequest {
    private String symbol;      // e.g., "AAPL"
    private String name;        // e.g., "Apple Inc." (needed for BUYing a new asset)
    private int quantity;       // e.g., 10
    private String tradeType;   // Must be "BUY" or "SELL"
}
