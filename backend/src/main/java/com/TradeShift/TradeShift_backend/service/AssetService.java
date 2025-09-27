package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AssetService {
    @Transactional
        // *** IMPORTANT: The `price` parameter is now removed from the method signature ***
    Asset buyAsset(Portfolio portfolio, String symbol, String name, int quantity) throws Exception;

    @Transactional
        // *** IMPORTANT: Remove `price` from the method signature here too ***
    Asset sellAsset(Portfolio portfolio, String symbol, int quantity) throws Exception;

    Asset buyAsset(Portfolio portfolio, String symbol, String name, int quantity, double price) throws Exception;
    Asset sellAsset(Portfolio portfolio, String symbol, int quantity, double price) throws Exception;
    List<Asset> getAssetsForPortfolio(Portfolio portfolio);
}
