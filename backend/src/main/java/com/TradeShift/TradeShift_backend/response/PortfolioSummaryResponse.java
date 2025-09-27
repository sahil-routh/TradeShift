package com.TradeShift.TradeShift_backend.response;

import com.TradeShift.TradeShift_backend.model.Asset;
import lombok.Data;
import java.util.List;

@Data
public class PortfolioSummaryResponse {
    private double cashBalance;
    private double totalPortfolioValue;
    private List<Asset> assets;
}