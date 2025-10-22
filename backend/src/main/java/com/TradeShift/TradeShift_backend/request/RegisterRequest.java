// com.TradeShift.TradeShift_backend.request.RegisterRequest.java
package com.TradeShift.TradeShift_backend.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    // Add any other registration fields you need
}