package com.TradeShift.TradeShift_backend.request;

import lombok.Data;

@Data
public class SellAssetRequest {
    private String symbol;
    private int quantity;
     // price used for selling
}

