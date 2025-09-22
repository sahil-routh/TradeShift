package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.Asset;
import com.TradeShift.TradeShift_backend.Model.Portfolio;
import com.TradeShift.TradeShift_backend.Model.User;
import com.TradeShift.TradeShift_backend.repository.AssetRepository;
import com.TradeShift.TradeShift_backend.repository.PortfolioRepository;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Portfolio createPortfolioForUser(User user) {
        Portfolio p = new Portfolio();
        p.setUser(user);
        p.setCashBalance(0.0);
        p.setTotalValue(0.0);
        return portfolioRepository.save(p);
    }

    @Override
    public Portfolio getPortfolioForUser(User user) {
        Portfolio p = portfolioRepository.findByUser(user);
        if (p == null) {
            p = createPortfolioForUser(user);
        }
        recalculatePortfolioValue(p);
        return p;
    }

    @Override
    public Portfolio getPortfolioForEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return null;
        return getPortfolioForUser(user);
    }

    @Override
    public void recalculatePortfolioValue(Portfolio portfolio) {
        List<Asset> assets = portfolio.getAssets();
        double total = portfolio.getCashBalance();
        if (assets != null) {
            for (Asset a : assets) {
                double cp = a.getCurrentPrice();
                total += a.getQuantity() * cp;
            }
        }
        portfolio.setTotalValue(total);
        portfolioRepository.save(portfolio);
    }
}
