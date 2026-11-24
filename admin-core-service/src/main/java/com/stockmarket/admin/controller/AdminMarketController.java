package com.stockmarket.admin.controller;

import com.stockmarket.admin.dto.ApiDtos.*;
import com.stockmarket.admin.service.AdminMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Admin Stock & Exchange API", description = "CRUD for Users, Exchanges, Stocks, Excel Bulk Upload/Download, and Timestamped Prices")
public class AdminMarketController {

    private static final Logger log = LoggerFactory.getLogger(AdminMarketController.class);

    private final AdminMarketService service;

    public AdminMarketController(AdminMarketService service) {
        this.service = service;
    }

    // --- Admin User Endpoints ---
    @PostMapping("/admin/users")
    @Operation(summary = "Add a new user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest req) {
        log.info("REST request to create user {}", req.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(req));
    }

    @PutMapping("/admin/users/{id}")
    @Operation(summary = "Modify user details")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        log.info("REST request to update user {}", id);
        return ResponseEntity.ok(service.updateUser(id, req));
    }

    @DeleteMapping("/admin/users/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user {}", id);
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/users")
    @Operation(summary = "View all users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    // --- Exchange Endpoints ---
    @PostMapping("/exchanges")
    @Operation(summary = "Add a stock exchange")
    public ResponseEntity<ExchangeResponse> createExchange(@Valid @RequestBody ExchangeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createExchange(req));
    }

    @PutMapping("/exchanges/{id}")
    @Operation(summary = "Edit a stock exchange")
    public ResponseEntity<ExchangeResponse> updateExchange(@PathVariable Long id, @Valid @RequestBody ExchangeRequest req) {
        return ResponseEntity.ok(service.updateExchange(id, req));
    }

    @DeleteMapping("/exchanges/{id}")
    @Operation(summary = "Delete a stock exchange")
    public ResponseEntity<Void> deleteExchange(@PathVariable Long id) {
        service.deleteExchange(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exchanges")
    @Operation(summary = "View all stock exchanges")
    public ResponseEntity<List<ExchangeResponse>> getExchanges() {
        return ResponseEntity.ok(service.getAllExchanges());
    }

    // --- Stock Endpoints ---
    @PostMapping("/stocks")
    @Operation(summary = "Add a stock to an exchange")
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody StockRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createStock(req));
    }

    @PutMapping("/stocks/{id}")
    @Operation(summary = "Edit stock details")
    public ResponseEntity<StockResponse> updateStock(@PathVariable Long id, @Valid @RequestBody StockRequest req) {
        return ResponseEntity.ok(service.updateStock(id, req));
    }

    @DeleteMapping("/stocks/{id}")
    @Operation(summary = "Delete stock")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        service.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stocks")
    @Operation(summary = "View all stocks or filter by exchangeId")
    public ResponseEntity<List<StockResponse>> getStocks(@RequestParam(required = false) Long exchangeId) {
        List<StockResponse> result = (exchangeId != null)
                ? service.getStocksByExchangeId(exchangeId)
                : service.getAllStocks();
        return ResponseEntity.ok(result);
    }

    // --- Timestamped Stock Price Upload & History ---
    @PostMapping("/stocks/prices")
    @Operation(summary = "Admin upload stock price at a specific timestamp")
    public ResponseEntity<StockPricePointResponse> uploadPrice(@Valid @RequestBody StockPriceUploadRequest req) {
        log.info("Uploading price point for {}:{} at {}", req.exchangeCode(), req.symbol(), req.recordedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadStockPrice(req));
    }

    @GetMapping("/stocks/prices/history")
    @Operation(summary = "Get timestamped price points for a stock on an exchange")
    public ResponseEntity<List<StockPricePointResponse>> getHistory(
            @RequestParam String exchangeCode,
            @RequestParam String symbol,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return ResponseEntity.ok(service.getPriceHistory(exchangeCode, symbol, from, to));
    }

    // --- Excel Bulk Upload & Download ---
    @PostMapping(value = "/stocks/excel/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk upload stocks from Excel (.xlsx)")
    public ResponseEntity<List<StockResponse>> uploadExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.bulkUploadStocksFromExcel(file));
    }

    @GetMapping("/stocks/excel/download")
    @Operation(summary = "Download all stocks as Excel (.xlsx)")
    public ResponseEntity<byte[]> downloadExcel() {
        byte[] content = service.exportStocksToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("stocks.xlsx").build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
