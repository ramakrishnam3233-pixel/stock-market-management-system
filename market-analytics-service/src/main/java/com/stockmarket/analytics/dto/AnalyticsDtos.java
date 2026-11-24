package com.stockmarket.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AnalyticsDtos {

    public record PricePointDto(Long id, String exchangeCode, String symbol, BigDecimal price, Long volume, Instant recordedAt) {}

    public record ChartSeriesPoint(Instant timestamp, BigDecimal price, BigDecimal movingAverage, Long volume) {}

    public record PerformanceChartResponse(
            String exchangeCode,
            String symbol,
            String chartType,
            BigDecimal periodStartPrice,
            BigDecimal periodEndPrice,
            BigDecimal percentageChange,
            List<ChartSeriesPoint> series
    ) {}

    public record PriceUpdateEventDto(
            Long stockId, String exchangeCode, String symbol,
            BigDecimal price, Long volume, Instant recordedAt
    ) {}
}
