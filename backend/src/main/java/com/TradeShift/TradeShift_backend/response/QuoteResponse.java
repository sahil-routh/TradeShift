package com.TradeShift.TradeShift_backend.response;

import lombok.Data;

@Data
public class QuoteResponse {
    private double c;  // current price
    private double h;  // high price of the day
    private double l;  // low price of the day
    private double o;  // open price of the day
    private double pc; // previous close price
}