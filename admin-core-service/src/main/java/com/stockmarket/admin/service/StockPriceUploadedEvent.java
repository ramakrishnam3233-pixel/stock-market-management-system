package com.stockmarket.admin.service;

import java.math.BigDecimal;
import java.time.Instant;

public record StockPriceUploadedEvent(
        Long stockId,
        String exchangeCode,
        String symbol,
        BigDecimal price,
        Long volume,
        Instant recordedAt
) {}
