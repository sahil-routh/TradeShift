package com.TradeShift.TradeShift_backend.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Adds a no-argument constructor
@AllArgsConstructor // Adds a constructor with all fields
public class ApiResponse {
    private boolean status;
    private String message;

    // This is the constructor your code is looking for
    public ApiResponse(String message) {
        this.message = message;
    }
}