package com.TradeShift.TradeShift_backend.service;

import com.TradeShift.TradeShift_backend.response.QuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class FinnhubApiService {

    @Value("${finnhub.api.key}")
    private String apiKey;

    private final WebClient webClient;

    // Constructor to inject the WebClient builder and configure the base URL
    public FinnhubApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build();
    }

    // Method to get a real-time stock quote for a given symbol
    public Mono<QuoteResponse> getStockQuote(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("token", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(QuoteResponse.class);
    }
}