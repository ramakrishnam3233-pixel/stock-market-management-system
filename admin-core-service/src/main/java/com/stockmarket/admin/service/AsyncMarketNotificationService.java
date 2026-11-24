package com.stockmarket.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AsyncMarketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncMarketNotificationService.class);

    private final RestClient restClient;
    private final String analyticsBaseUrl;

    public AsyncMarketNotificationService(
            RestClient.Builder builder,
            @Value("${app.analytics-service.base-url:http://localhost:8082}") String analyticsBaseUrl) {
        this.restClient = builder.build();
        this.analyticsBaseUrl = analyticsBaseUrl;
    }

    @Async
    @EventListener
    public void onStockPriceUploaded(StockPriceUploadedEvent event) {
        log.info("Async event received: exchange={}, symbol={}, price={}, recordedAt={}",
                event.exchangeCode(), event.symbol(), event.price(), event.recordedAt());
        try {
            restClient.post()
                    .uri(analyticsBaseUrl + "/api/v1/analytics/events/price-updates")
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Async notification forwarded to analytics service for {}", event.symbol());
        } catch (Exception ex) {
            // Resilient async boundary so core price ingestion never fails if analytics is starting up
            log.warn("Async analytics notification deferred for {} due to: {}", event.symbol(), ex.getMessage());
        }
    }
}
