package com.TradeShift.TradeShift_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home()
    {

        return "Welcome to Trade Shift";
    }
    @GetMapping("/api")
    public String secure()
    {

        return "Welcome to Trade Shift secure";
    }
}

