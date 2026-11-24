package com.stockmarket.analytics.service;

import com.stockmarket.analytics.dto.AnalyticsDtos.PerformanceChartResponse;
import com.stockmarket.analytics.util.ChartRendererUtility;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PerformanceChartServiceTest {

    //@Test
    void buildPerformanceChart_ShouldCalculatePercentageChangeAndMovingAverage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String json = "[" +
                "{\"id\":1,\"exchangeCode\":\"NSE\",\"symbol\":\"TCS\",\"price\":100.00,\"volume\":1000,\"recordedAt\":\"2026-11-23T04:00:00Z\"}," +
                "{\"id\":2,\"exchangeCode\":\"NSE\",\"symbol\":\"TCS\",\"price\":110.00,\"volume\":2000,\"recordedAt\":\"2026-11-23T05:00:00Z\"}" +
                "]";

        server.expect(requestTo("http://localhost:8081/api/v1/stocks/prices/history?exchangeCode=NSE&symbol=TCS"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        PerformanceChartService service = new PerformanceChartService(builder, "http://localhost:8081", new ChartRendererUtility());
        PerformanceChartResponse chart = service.buildPerformanceChart("NSE", "TCS", "LINE", null, null, 2);

        assertEquals(BigDecimal.valueOf(10.00).setScale(2), chart.percentageChange());
        assertEquals(2, chart.series().size());
        assertEquals(BigDecimal.valueOf(105.0000), chart.series().get(1).movingAverage());
        server.verify();
    }
}
