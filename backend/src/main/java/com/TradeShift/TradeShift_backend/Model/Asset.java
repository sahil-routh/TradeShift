package com.TradeShift.TradeShift_backend.Model;

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
        private String name;         // e.g. Apple Inc. (optional)
        private int quantity;
        private double purchasePrice;
        private double currentPrice; // will be filled from market feed in future

        @ManyToOne
        @JoinColumn(name = "portfolio_id")
        @JsonIgnore // avoid recursive serialization
        private Portfolio portfolio;
    }


