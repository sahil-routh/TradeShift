package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.Transaction;
import com.TradeShift.TradeShift_backend.repository.AssetRepository;
import com.TradeShift.TradeShift_backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private FinnhubApiService finnhubApiService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    @Override
    public Asset buyAsset(Portfolio portfolio, String symbol, String name, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        double price = finnhubApiService.getStockQuote(symbol).block().getC();
        if (price <= 0) {
            throw new Exception("Could not fetch real-time price for " + symbol);
        }

        double totalCost = quantity * price;
        if (portfolio.getCashBalance() < totalCost) {
            throw new Exception("Insufficient funds in portfolio. Deposit more cash or reduce quantity.");
        }

        Optional<Asset> opt = assetRepository.findByPortfolioAndSymbol(portfolio, symbol);
        Asset asset;
        if (opt.isPresent()) {
            asset = opt.get();
            double existingValue = asset.getAvgBuyPrice() * asset.getQuantity();
            double newValue = price * quantity;
            int newQty = asset.getQuantity() + quantity;
            double newAvg = (existingValue + newValue) / newQty;

            asset.setQuantity(newQty);
            asset.setAvgBuyPrice(newAvg);
            asset.setCurrentPrice(price);
        } else {
            asset = new Asset();
            asset.setPortfolio(portfolio);
            asset.setSymbol(symbol);
            asset.setName(name);
            asset.setQuantity(quantity);
            asset.setAvgBuyPrice(price);
            asset.setCurrentPrice(price);
        }

        // Deduct cash
        portfolio.setCashBalance(portfolio.getCashBalance() - totalCost);

        Asset saved = assetRepository.save(asset);

        // Save BUY transaction
        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setSymbol(symbol);
        transaction.setTransactionType("BUY");
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        portfolioService.recalculatePortfolioValue(portfolio);
        return saved;
    }

    @Transactional
    @Override
    public Asset sellAsset(Portfolio portfolio, String symbol, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        double price = finnhubApiService.getStockQuote(symbol).block().getC();
        if (price <= 0) {
            throw new Exception("Could not fetch real-time price for " + symbol);
        }

        Optional<Asset> opt = assetRepository.findByPortfolioAndSymbol(portfolio, symbol);
        if (opt.isEmpty()) {
            throw new Exception("You do not own this asset.");
        }

        Asset asset = opt.get();
        if (asset.getQuantity() < quantity) {
            throw new Exception("Not enough quantity to sell.");
        }

        int remaining = asset.getQuantity() - quantity;
        double proceeds = quantity * price;

        if (remaining == 0) {
            // Instead of deleting, keep the asset with 0 quantity
            asset.setQuantity(0);
            asset.setCurrentPrice(price);
        } else {
            asset.setQuantity(remaining);
            asset.setCurrentPrice(price);
        }

        // Credit cash
        portfolio.setCashBalance(portfolio.getCashBalance() + proceeds);

        Asset updatedAsset = assetRepository.save(asset);

        // Save SELL transaction
        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setSymbol(symbol);
        transaction.setTransactionType("SELL");
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        portfolioService.recalculatePortfolioValue(portfolio);

        return updatedAsset;
    }

    @Override
    public Asset buyAsset(Portfolio portfolio, String symbol, String name, int quantity, double price) throws Exception {
        throw new UnsupportedOperationException("Use the real-time price buyAsset method instead.");
    }

    @Override
    public Asset sellAsset(Portfolio portfolio, String symbol, int quantity, double price) throws Exception {
        throw new UnsupportedOperationException("Use the real-time price sellAsset method instead.");
    }

    @Override
    public List<Asset> getAssetsForPortfolio(Portfolio portfolio) {
        return assetRepository.findByPortfolio(portfolio);
    }
}
