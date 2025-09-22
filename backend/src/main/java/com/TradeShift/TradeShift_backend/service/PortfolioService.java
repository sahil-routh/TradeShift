package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.Model.Portfolio;
import com.TradeShift.TradeShift_backend.Model.User;

public interface PortfolioService {
    Portfolio createPortfolioForUser(User user);
    Portfolio getPortfolioForUser(User user);
    Portfolio getPortfolioForEmail(String email);
    void recalculatePortfolioValue(Portfolio portfolio);
}
