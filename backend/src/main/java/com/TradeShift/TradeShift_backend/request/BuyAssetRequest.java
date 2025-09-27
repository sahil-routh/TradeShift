package com.TradeShift.TradeShift_backend.request;

import lombok.Data;

@Data
public class BuyAssetRequest {
    private String symbol;
    private String name; // optional
    private int quantity;
     // price used for this buy (can come from UI or market API)
}
