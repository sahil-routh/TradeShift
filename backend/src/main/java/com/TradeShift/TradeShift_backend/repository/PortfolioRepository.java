
package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Portfolio findByUser(User user);
}
