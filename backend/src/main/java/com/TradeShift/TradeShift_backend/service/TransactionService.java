package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.model.Portfolio;
import com.TradeShift.TradeShift_backend.model.Transaction;
import com.TradeShift.TradeShift_backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsForPortfolio(Portfolio portfolio) {
        return transactionRepository.findByPortfolioOrderByTimestampDesc(portfolio);
    }
}