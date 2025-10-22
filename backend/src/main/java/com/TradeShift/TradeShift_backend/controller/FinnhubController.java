package com.TradeShift.TradeShift_backend.controller;

import com.TradeShift.TradeShift_backend.response.QuoteResponse;
import com.TradeShift.TradeShift_backend.service.FinnhubApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finnhub")
@CrossOrigin(origins = "http://localhost:5173")
public class FinnhubController {

    @Autowired
    private FinnhubApiService finnhubApiService;

    /**
     * Get real-time quote for a single stock
     * GET /api/finnhub/quote/{symbol}
     */
    @GetMapping("/quote/{symbol}")
    public Mono<ResponseEntity<QuoteResponse>> getStockQuote(@PathVariable String symbol) {
        System.out.println("📊 Fetching quote for: " + symbol);

        return finnhubApiService.getStockQuote(symbol.toUpperCase())
                .map(quote -> {
                    System.out.println("✅ Quote received for " + symbol + ": $" + quote.getC());
                    return ResponseEntity.ok(quote);
                })
                .onErrorResume(e -> {
                    System.err.println("❌ Error fetching quote for " + symbol + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }

    /**
     * Get multiple stock quotes for major stocks or watchlist
     * GET /api/finnhub/major-stocks?symbols=AAPL,MSFT,GOOGL
     */
    @GetMapping("/major-stocks")
    public ResponseEntity<List<Map<String, Object>>> getMajorStocks(
            @RequestParam(required = false, defaultValue = "AAPL,MSFT,GOOGL,AMZN,TSLA") String symbols) {

        System.out.println("📊 Fetching major stocks: " + symbols);

        String[] symbolArray = symbols.split(",");
        List<Map<String, Object>> results = new ArrayList<>();

        for (String symbol : symbolArray) {
            try {
                QuoteResponse quote = finnhubApiService.getStockQuote(symbol.trim().toUpperCase()).block();

                if (quote != null && quote.getC() > 0) {
                    Map<String, Object> stockData = new HashMap<>();
                    stockData.put("symbol", symbol.trim().toUpperCase());
                    stockData.put("price", quote.getC());  // Current price
                    stockData.put("change", quote.getD());  // Change
                    stockData.put("changePercent", quote.getDp());  // Change percent
                    stockData.put("high", quote.getH());  // High
                    stockData.put("low", quote.getL());  // Low
                    stockData.put("open", quote.getO());  // Open
                    stockData.put("previousClose", quote.getPc());  // Previous close

                    results.add(stockData);
                    System.out.println("✅ " + symbol + ": $" + quote.getC());
                }
            } catch (Exception e) {
                System.err.println("❌ Error fetching " + symbol + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Search stocks by symbol or company name
     * GET /api/finnhub/search?query=AAPL
     * Note: This is a basic implementation. For production, you'd want to use
     * Finnhub's /search endpoint or maintain a local database of stock symbols.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, String>>> searchStocks(@RequestParam String query) {
        System.out.println("🔍 Searching for: " + query);

        // For now, return a predefined list of popular stocks that match the query
        // In production, you'd query a database or use Finnhub's symbol lookup API
        List<Map<String, String>> results = new ArrayList<>();

        String[][] popularStocks = {
                {"AAPL", "Apple Inc."},
                {"MSFT", "Microsoft Corporation"},
                {"GOOGL", "Alphabet Inc. (Google)"},
                {"AMZN", "Amazon.com Inc."},
                {"TSLA", "Tesla Inc."},
                {"META", "Meta Platforms Inc. (Facebook)"},
                {"NVDA", "NVIDIA Corporation"},
                {"JPM", "JPMorgan Chase & Co."},
                {"V", "Visa Inc."},
                {"WMT", "Walmart Inc."},
                {"DIS", "The Walt Disney Company"},
                {"NFLX", "Netflix Inc."},
                {"PYPL", "PayPal Holdings Inc."},
                {"INTC", "Intel Corporation"},
                {"CSCO", "Cisco Systems Inc."},
                {"PEP", "PepsiCo Inc."},
                {"KO", "The Coca-Cola Company"},
                {"NKE", "Nike Inc."},
                {"BA", "The Boeing Company"},
                {"ADBE", "Adobe Inc."},
                {"ORCL", "Oracle Corporation"},
                {"IBM", "International Business Machines Corporation"},
                {"CRM", "Salesforce Inc."},
                {"AMD", "Advanced Micro Devices Inc."},
                {"XOM", "Exxon Mobil Corporation"},
                {"CVX", "Chevron Corporation"},
                {"PFE", "Pfizer Inc."},
                {"ABT", "Abbott Laboratories"},
                {"T", "AT&T Inc."},
                {"VZ", "Verizon Communications Inc."},
                {"UBER", "Uber Technologies Inc."},
                {"LYFT", "Lyft Inc."},
                {"SHOP", "Shopify Inc."},
                {"SQ", "Block Inc. (Square)"},
                {"BABA", "Alibaba Group Holding Ltd."},
                {"SONY", "Sony Group Corporation"},
                {"TSM", "Taiwan Semiconductor Manufacturing Company"},
                {"NIO", "NIO Inc."},
                {"DELL", "Dell Technologies Inc."},
                {"HPQ", "HP Inc."},
                {"C", "Citigroup Inc."},
                {"GS", "Goldman Sachs Group Inc."},
                {"MS", "Morgan Stanley"},
                {"SPOT", "Spotify Technology S.A."},
                {"INTU", "Intuit Inc."},
                {"MCD", "McDonald's Corporation"},
                {"SBUX", "Starbucks Corporation"},
                {"TMO", "Thermo Fisher Scientific Inc."},
                {"LLY", "Eli Lilly and Company"},
                {"UNH", "UnitedHealth Group Incorporated"}
        };


        String searchQuery = query.toLowerCase();
        for (String[] stock : popularStocks) {
            if (stock[0].toLowerCase().contains(searchQuery) ||
                    stock[1].toLowerCase().contains(searchQuery)) {
                Map<String, String> result = new HashMap<>();
                result.put("symbol", stock[0]);
                result.put("name", stock[1]);
                results.add(result);
            }
        }

        System.out.println("✅ Found " + results.size() + " results for: " + query);
        return ResponseEntity.ok(results);
    }

    /**
     * Get live price for immediate trade execution
     * GET /api/finnhub/live-price/{symbol}
     */
    @GetMapping("/live-price/{symbol}")
    public Mono<ResponseEntity<Map<String, Object>>> getLivePrice(@PathVariable String symbol) {
        return finnhubApiService.getStockQuote(symbol.toUpperCase())
                .map(quote -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("symbol", symbol.toUpperCase());
                    response.put("price", quote.getC());
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Could not fetch live price for " + symbol);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                });
    }
}