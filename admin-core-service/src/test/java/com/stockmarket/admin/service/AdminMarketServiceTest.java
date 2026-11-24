package com.stockmarket.admin.service;

import com.stockmarket.admin.domain.*;
import com.stockmarket.admin.dto.ApiDtos.*;
import com.stockmarket.admin.exception.CustomExceptions.*;
import com.stockmarket.admin.repository.*;
import com.stockmarket.admin.util.ExcelStockUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMarketServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ExchangeRepository exchangeRepository;
    @Mock private StockRepository stockRepository;
    @Mock private StockPricePointRepository pricePointRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ExcelStockUtility excelStockUtility;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdminMarketService service;

    private ExchangeEntity exchange;
    private StockEntity stock;

    @BeforeEach
    void setUp() {
        exchange = new ExchangeEntity();
        exchange.setId(1L);
        exchange.setCode("NASDAQ");
        exchange.setName("NASDAQ Stock Market");
        exchange.setCountry("USA");
        exchange.setCurrency("USD");

        stock = new StockEntity();
        stock.setId(10L);
        stock.setSymbol("AAPL");
        stock.setCompanyName("Apple Inc.");
        stock.setSector("Technology");
        stock.setLatestPrice(BigDecimal.valueOf(210.50));
        stock.setExchange(exchange);
    }

    @Test
    void uploadStockPrice_ShouldSavePointUpdateStockAndPublishEvent() {
        Instant ts = Instant.parse("2026-11-23T09:30:00Z");
        when(stockRepository.findBySymbolIgnoreCaseAndExchange_CodeIgnoreCase("AAPL", "NASDAQ"))
                .thenReturn(Optional.of(stock));
        when(pricePointRepository.save(any(StockPricePointEntity.class))).thenAnswer(inv -> {
            StockPricePointEntity p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(stockRepository.save(any(StockEntity.class))).thenReturn(stock);

        StockPriceUploadRequest req = new StockPriceUploadRequest("NASDAQ", "AAPL", BigDecimal.valueOf(215.25), 5000L, ts);
        StockPricePointResponse res = service.uploadStockPrice(req);

        assertEquals(99L, res.id());
        assertEquals(BigDecimal.valueOf(215.25), res.price());
        assertEquals(ts, res.recordedAt());
        verify(eventPublisher, times(1)).publishEvent(any(StockPriceUploadedEvent.class));
    }

    @Test
    void uploadStockPrice_WhenStockMissing_ShouldThrowResourceNotFound() {
        when(stockRepository.findBySymbolIgnoreCaseAndExchange_CodeIgnoreCase("MSFT", "NASDAQ"))
                .thenReturn(Optional.empty());

        StockPriceUploadRequest req = new StockPriceUploadRequest("NASDAQ", "MSFT", BigDecimal.TEN, 100L, Instant.now());
        assertThrows(ResourceNotFoundException.class, () -> service.uploadStockPrice(req));
    }

    @Test
    void createUser_WhenDuplicateEmail_ShouldThrowDuplicateResourceException() {
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);
        UserCreateRequest req = new UserCreateRequest("Admin", "admin@test.com", "secret123", Role.ROLE_ADMIN);
        assertThrows(DuplicateResourceException.class, () -> service.createUser(req));
    }
}
