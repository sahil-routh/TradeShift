package com.TradeShift.TradeShift_backend.response;

import lombok.Data;

@Data
public class AssetSummaryResponse {
    private Long id;
    private String symbol;
    private String name;
    private int quantity;
    private double avgBuyPrice;
    private double currentPrice;
    private double marketValue;
    private double profitLoss;
    private double realizedPL;
}
