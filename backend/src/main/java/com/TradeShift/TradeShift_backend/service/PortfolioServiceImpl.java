package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.repository.AssetRepository;
import com.TradeShift.TradeShift_backend.repository.PortfolioRepository;
import com.TradeShift.TradeShift_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for transactional consistency

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    // Note: AssetRepository and UserRepository are injected but not used in the final methods,
    // which is fine, but they could be removed if not needed elsewhere in this class.

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AssetRepository assetRepository; // Kept, assuming it's used for asset-related logic

    @Autowired
    private UserRepository userRepository; // Kept

    // --- FIX 1: Rename/Override for consistency with the required interface method ---
    // This signature matches the expected behavior for new user registration.
    @Transactional
    @Override
    public Portfolio createPortfolio(Portfolio portfolio) {
        // Ensure starting cash balance is set upon registration
        if (portfolio.getCashBalance() == 0.0) {
            portfolio.setCashBalance(10000.00);
            portfolio.setTotalValue(10000.00); // Initial cash = initial total value
        }
        return portfolioRepository.save(portfolio);
    }

    // --- Original createPortfolioForUser logic, renamed and adapted ---
    // NOTE: If you need both methods, you must define both in the PortfolioService interface.
    // For now, I'm adapting your original logic for a safe initialization, but keeping the name 'createPortfolio' for dependency resolution.

    // Original method body adapted to a safe initialization method
    public Portfolio safeCreatePortfolioForUser(User user) {
        Portfolio p = new Portfolio();
        p.setUser(user);
        p.setCashBalance(10000.00); // Initial starting balance
        p.setTotalValue(10000.00);
        return portfolioRepository.save(p);
    }
    // ------------------------------------------------------------------

    @Override
    public Portfolio getPortfolioForUser(User user) {
        // FIX 2: Use Optional<Portfolio> as is standard for JpaRepository.findByX()
        Optional<Portfolio> pOpt = Optional.ofNullable(portfolioRepository.findByUser(user));

        Portfolio p;
        if (pOpt.isEmpty()) {
            // FIX 3: Use the safe creation method when no portfolio is found.
            p = safeCreatePortfolioForUser(user);
        } else {
            p = pOpt.get();
        }

        recalculatePortfolioValue(p);
        return p;
    }

    @Override
    public Portfolio getPortfolioForEmail(String email) {
        // Assuming findByEmail is correctly implemented in UserRepository
        User user = userRepository.findByEmail(email);
        if (user == null) return null;
        return getPortfolioForUser(user);
    }

    // This method is correctly structured for recalculation logic
    @Override
    public void recalculatePortfolioValue(Portfolio portfolio) {
        List<Asset> assets = portfolio.getAssets();
        double total = portfolio.getCashBalance();

        // This logic is correct for updating total value based on current cash + asset market value
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