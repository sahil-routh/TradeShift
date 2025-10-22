package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;

public interface PortfolioService {
    Portfolio createPortfolio(Portfolio portfolio);
    Portfolio getPortfolioForUser(User user);
    Portfolio getPortfolioForEmail(String email);
    void recalculatePortfolioValue(Portfolio portfolio);
}
