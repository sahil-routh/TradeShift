
package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.Model.Portfolio;
import com.TradeShift.TradeShift_backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Portfolio findByUser(User user);
}
