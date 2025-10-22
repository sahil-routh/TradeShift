package com.TradeShift.TradeShift_backend.repository;

import com.TradeShift.TradeShift_backend.model.User;
import com.TradeShift.TradeShift_backend.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUser(User user);

    List<Watchlist> findByUserOrderByAddedAtDesc(User user);

    Optional<Watchlist> findByUserAndSymbol(User user, String symbol);

    boolean existsByUserAndSymbol(User user, String symbol);

    void deleteByUserAndSymbol(User user, String symbol);
}