package com.stockmarket.admin.service;

import com.stockmarket.admin.domain.*;
import com.stockmarket.admin.dto.ApiDtos.*;
import com.stockmarket.admin.exception.CustomExceptions.*;
import com.stockmarket.admin.repository.*;
import com.stockmarket.admin.util.ExcelStockUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AdminMarketService {

    private static final Logger log = LoggerFactory.getLogger(AdminMarketService.class);

    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;
    private final StockRepository stockRepository;
    private final StockPricePointRepository pricePointRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelStockUtility excelStockUtility;
    private final ApplicationEventPublisher eventPublisher;

    public AdminMarketService(
            UserRepository userRepository,
            ExchangeRepository exchangeRepository,
            StockRepository stockRepository,
            StockPricePointRepository pricePointRepository,
            PasswordEncoder passwordEncoder,
            ExcelStockUtility excelStockUtility,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.exchangeRepository = exchangeRepository;
        this.stockRepository = stockRepository;
        this.pricePointRepository = pricePointRepository;
        this.passwordEncoder = passwordEncoder;
        this.excelStockUtility = excelStockUtility;
        this.eventPublisher = eventPublisher;
    }

    // --- User Service ---
    public UserResponse createUser(UserCreateRequest req) {
        log.info("Creating user with email={}", req.email());
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("User already exists with email: " + req.email());
        }
        UserEntity user = new UserEntity();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setActive(true);
        UserEntity saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest req) {
        log.info("Updating user id={}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setFullName(req.fullName());
        user.setRole(req.role());
        user.setActive(req.active());
        return toUserResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    // --- Exchange Service ---
    public ExchangeResponse createExchange(ExchangeRequest req) {
        log.info("Creating exchange code={}", req.code());
        if (exchangeRepository.existsByCodeIgnoreCase(req.code())) {
            throw new DuplicateResourceException("Exchange already exists: " + req.code());
        }
        ExchangeEntity ex = new ExchangeEntity();
        ex.setCode(req.code().toUpperCase());
        ex.setName(req.name());
        ex.setCountry(req.country());
        ex.setCurrency(req.currency().toUpperCase());
        return toExchangeResponse(exchangeRepository.save(ex));
    }

    public ExchangeResponse updateExchange(Long id, ExchangeRequest req) {
        log.info("Updating exchange id={}", id);
        ExchangeEntity ex = exchangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found: " + id));
        ex.setCode(req.code().toUpperCase());
        ex.setName(req.name());
        ex.setCountry(req.country());
        ex.setCurrency(req.currency().toUpperCase());
        return toExchangeResponse(exchangeRepository.save(ex));
    }

    public void deleteExchange(Long id) {
        log.info("Deleting exchange id={}", id);
        if (!exchangeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exchange not found: " + id);
        }
        exchangeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getAllExchanges() {
        return exchangeRepository.findAll().stream().map(this::toExchangeResponse).toList();
    }

    // --- Stock Service ---
    public StockResponse createStock(StockRequest req) {
        log.info("Creating stock symbol={} on exchangeId={}", req.symbol(), req.exchangeId());
        ExchangeEntity exchange = exchangeRepository.findById(req.exchangeId())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found: " + req.exchangeId()));
        if (stockRepository.existsBySymbolIgnoreCaseAndExchangeId(req.symbol(), exchange.getId())) {
            throw new DuplicateResourceException("Stock " + req.symbol() + " already listed on " + exchange.getCode());
        }
        StockEntity stock = new StockEntity();
        stock.setSymbol(req.symbol().toUpperCase());
        stock.setCompanyName(req.companyName());
        stock.setSector(req.sector());
        stock.setLatestPrice(req.latestPrice());
        stock.setExchange(exchange);
        StockEntity saved = stockRepository.save(stock);

        // Seed initial timestamped price point
        StockPricePointEntity initialPoint = new StockPricePointEntity();
        initialPoint.setStock(saved);
        initialPoint.setPrice(saved.getLatestPrice());
        initialPoint.setVolume(0L);
        initialPoint.setRecordedAt(Instant.now());
        pricePointRepository.save(initialPoint);

        return toStockResponse(saved);
    }

    public StockResponse updateStock(Long id, StockRequest req) {
        log.info("Updating stock id={}", id);
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + id));
        ExchangeEntity exchange = exchangeRepository.findById(req.exchangeId())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found: " + req.exchangeId()));
        stock.setSymbol(req.symbol().toUpperCase());
        stock.setCompanyName(req.companyName());
        stock.setSector(req.sector());
        stock.setLatestPrice(req.latestPrice());
        stock.setExchange(exchange);
        return toStockResponse(stockRepository.save(stock));
    }

    public void deleteStock(Long id) {
        log.info("Deleting stock id={}", id);
        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock not found: " + id);
        }
        stockRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream().map(this::toStockResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStocksByExchangeId(Long exchangeId) {
        return stockRepository.findByExchangeId(exchangeId).stream().map(this::toStockResponse).toList();
    }

    // --- Timestamped Stock Price Upload & History ---
    public StockPricePointResponse uploadStockPrice(StockPriceUploadRequest req) {
        log.info("Uploading price for exchange={}, symbol={}, price={}, at={}",
                req.exchangeCode(), req.symbol(), req.price(), req.recordedAt());
        StockEntity stock = stockRepository
                .findBySymbolIgnoreCaseAndExchange_CodeIgnoreCase(req.symbol(), req.exchangeCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock " + req.symbol() + " not found on exchange " + req.exchangeCode()));

        StockPricePointEntity point = new StockPricePointEntity();
        point.setStock(stock);
        point.setPrice(req.price());
        point.setVolume(req.volume());
        point.setRecordedAt(req.recordedAt());
        StockPricePointEntity savedPoint = pricePointRepository.save(point);

        stock.setLatestPrice(req.price());
        stockRepository.save(stock);

        eventPublisher.publishEvent(new StockPriceUploadedEvent(
                stock.getId(),
                stock.getExchange().getCode(),
                stock.getSymbol(),
                req.price(),
                req.volume(),
                req.recordedAt()
        ));

        return new StockPricePointResponse(
                savedPoint.getId(),
                stock.getExchange().getCode(),
                stock.getSymbol(),
                savedPoint.getPrice(),
                savedPoint.getVolume(),
                savedPoint.getRecordedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<StockPricePointResponse> getPriceHistory(String exchangeCode, String symbol, Instant from, Instant to) {
        StockEntity stock = stockRepository
                .findBySymbolIgnoreCaseAndExchange_CodeIgnoreCase(symbol, exchangeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock " + symbol + " not found on exchange " + exchangeCode));

        List<StockPricePointEntity> points = (from != null && to != null)
                ? pricePointRepository.findByStockIdAndRecordedAtBetweenOrderByRecordedAtAsc(stock.getId(), from, to)
                : pricePointRepository.findByStockIdOrderByRecordedAtAsc(stock.getId());

        return points.stream()
                .map(p -> new StockPricePointResponse(
                        p.getId(),
                        stock.getExchange().getCode(),
                        stock.getSymbol(),
                        p.getPrice(),
                        p.getVolume(),
                        p.getRecordedAt()))
                .toList();
    }

    // --- Excel Bulk Upload / Download ---
    public List<StockResponse> bulkUploadStocksFromExcel(MultipartFile file) {
        try {
            List<StockRequest> rows = excelStockUtility.parseStocksExcel(file.getInputStream());
            return rows.stream().map(this::createStock).toList();
        } catch (IOException e) {
            throw new InvalidExcelFormatException("Failed to read uploaded Excel file");
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportStocksToExcel() {
        List<StockRequest> rows = stockRepository.findAll().stream()
                .map(s -> new StockRequest(
                        s.getSymbol(),
                        s.getCompanyName(),
                        s.getSector(),
                        s.getLatestPrice(),
                        s.getExchange().getId()))
                .toList();
        return excelStockUtility.generateStocksExcel(rows);
    }

    private UserResponse toUserResponse(UserEntity u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt());
    }

    private ExchangeResponse toExchangeResponse(ExchangeEntity e) {
        return new ExchangeResponse(e.getId(), e.getCode(), e.getName(), e.getCountry(), e.getCurrency(), e.getCreatedAt());
    }

    private StockResponse toStockResponse(StockEntity s) {
        return new StockResponse(
                s.getId(), s.getSymbol(), s.getCompanyName(), s.getSector(),
                s.getLatestPrice(), s.getExchange().getId(), s.getExchange().getCode()
        );
    }
}
