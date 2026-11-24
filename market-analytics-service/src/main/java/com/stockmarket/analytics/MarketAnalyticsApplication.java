package com.stockmarket.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MarketAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketAnalyticsApplication.class, args);
    }
}
