package com.stockmarket.analytics.controller;

import com.stockmarket.analytics.dto.AnalyticsDtos.PerformanceChartResponse;
import com.stockmarket.analytics.dto.AnalyticsDtos.PriceUpdateEventDto;
import com.stockmarket.analytics.service.PerformanceChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "User Performance Chart Service", description = "Generate stock market performance chart data and PNG charts")
public class AnalyticsChartController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsChartController.class);

    private final PerformanceChartService chartService;

    public AnalyticsChartController(PerformanceChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/charts/performance")
    @Operation(summary = "Generate JSON performance chart series with moving averages")
    public ResponseEntity<PerformanceChartResponse> getPerformanceChart(
            @RequestParam String exchangeCode,
            @RequestParam String symbol,
            @RequestParam(defaultValue = "LINE_MA") String chartType,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "3") int windowSize) {
        log.info("Chart request received for {}:{} type={}", exchangeCode, symbol, chartType);
        return ResponseEntity.ok(chartService.buildPerformanceChart(exchangeCode, symbol, chartType, from, to, windowSize));
    }

    @GetMapping(value = "/charts/performance/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate PNG performance chart image")
    public ResponseEntity<byte[]> getPerformanceChartImage(
            @RequestParam String exchangeCode,
            @RequestParam String symbol,
            @RequestParam(defaultValue = "LINE") String chartType,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        byte[] png = chartService.renderChartImage(exchangeCode, symbol, chartType, from, to);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }

    @PostMapping("/events/price-updates")
    @Operation(summary = "Receive async stock price upload event from Admin Core Service")
    public ResponseEntity<Void> receivePriceUpdate(@RequestBody PriceUpdateEventDto event) {
        chartService.processPriceUpdateAsync(event);
        return ResponseEntity.accepted().build();
    }
}
