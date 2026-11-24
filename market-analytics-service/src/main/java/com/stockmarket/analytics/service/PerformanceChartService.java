package com.stockmarket.analytics.service;

import com.stockmarket.analytics.dto.AnalyticsDtos.*;
import com.stockmarket.analytics.util.ChartRendererUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PerformanceChartService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceChartService.class);

    private final RestClient restClient;
    private final String adminServiceBaseUrl;
    private final ChartRendererUtility chartRendererUtility;

    public PerformanceChartService(
            RestClient.Builder builder,
            @Value("${app.admin-service.base-url:http://localhost:8081}") String adminServiceBaseUrl,
            ChartRendererUtility chartRendererUtility) {
        this.restClient = builder.build();
        this.adminServiceBaseUrl = adminServiceBaseUrl;
        this.chartRendererUtility = chartRendererUtility;
    }

    public PerformanceChartResponse buildPerformanceChart(
            String exchangeCode, String symbol, String chartType, Instant from, Instant to, int windowSize) {
        log.info("Synchronous fetch of price history for {}:{} ({})", exchangeCode, symbol, chartType);
        List<PricePointDto> history = restClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/api/v1/stocks/prices/history")
                            .queryParam("exchangeCode", exchangeCode)
                            .queryParam("symbol", symbol);
                    if (from != null) b.queryParam("from", from);
                    if (to != null) b.queryParam("to", to);
                    return b.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<PricePointDto>>() {});

        List<PricePointDto> safeHistory = history == null ? List.of() : history;
        List<ChartSeriesPoint> series = new ArrayList<>();
        int effectiveWindow = Math.max(1, windowSize);

        for (int i = 0; i < safeHistory.size(); i++) {
            int startIdx = Math.max(0, i - effectiveWindow + 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = startIdx; j <= i; j++) {
                sum = sum.add(safeHistory.get(j).price());
            }
            BigDecimal ma = sum.divide(BigDecimal.valueOf(i - startIdx + 1), 4, RoundingMode.HALF_UP);
            PricePointDto p = safeHistory.get(i);
            series.add(new ChartSeriesPoint(p.recordedAt(), p.price(), ma, p.volume()));
        }

        BigDecimal startPrice = series.isEmpty() ? BigDecimal.ZERO : series.get(0).price();
        BigDecimal endPrice = series.isEmpty() ? BigDecimal.ZERO : series.get(series.size() - 1).price();
        BigDecimal pctChange = BigDecimal.ZERO;
        if (startPrice.compareTo(BigDecimal.ZERO) > 0) {
            pctChange = endPrice.subtract(startPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(startPrice, 2, RoundingMode.HALF_UP);
        }

        return new PerformanceChartResponse(
                exchangeCode.toUpperCase(), symbol.toUpperCase(), chartType.toUpperCase(),
                startPrice, endPrice, pctChange, series
        );
    }

    public byte[] renderChartImage(String exchangeCode, String symbol, String chartType, Instant from, Instant to) {
        PerformanceChartResponse chart = buildPerformanceChart(exchangeCode, symbol, chartType, from, to, 3);
        return chartRendererUtility.renderPngChart(chart, 900, 480);
    }

    @Async
    public void processPriceUpdateAsync(PriceUpdateEventDto event) {
        log.info("Async analytics ingestion for {}:{} at {} -> price={}",
                event.exchangeCode(), event.symbol(), event.recordedAt(), event.price());
    }
}
