package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.model.Asset;
import com.TradeShift.TradeShift_backend.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByPortfolioAndSymbol(Portfolio portfolio, String symbol);
    List<Asset> findByPortfolio(Portfolio portfolio);
}
