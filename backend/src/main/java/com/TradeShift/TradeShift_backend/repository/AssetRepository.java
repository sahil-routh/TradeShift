
package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.Model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByPortfolioId(Long portfolioId);
}
