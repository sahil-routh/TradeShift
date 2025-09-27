package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;

public interface PortfolioService {
    Portfolio createPortfolioForUser(User user);
    Portfolio getPortfolioForUser(User user);
    Portfolio getPortfolioForEmail(String email);
    void recalculatePortfolioValue(Portfolio portfolio);
}
