package com.TradeShift.TradeShift_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String symbol;       // e.g. AAPL
    private String name;         // optional display name
    private int quantity;        // total shares owned
    private double avgBuyPrice;  // average buy price (weighted)
    private double currentPrice; // latest market price (for value calc)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio;
}
