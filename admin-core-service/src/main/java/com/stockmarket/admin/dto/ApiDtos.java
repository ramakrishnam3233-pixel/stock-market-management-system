package com.stockmarket.admin.dto;

import com.stockmarket.admin.domain.Role;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ApiDtos {

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record JwtResponse(String token, String tokenType, String email, String role) {}

    public record UserCreateRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6) String password,
            @NotNull Role role
    ) {}

    public record UserUpdateRequest(
            @NotBlank String fullName,
            @NotNull Role role,
            boolean active
    ) {}

    public record UserResponse(Long id, String fullName, String email, Role role, boolean active, Instant createdAt) {}

    public record ExchangeRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String country,
            @NotBlank @Size(max = 10) String currency
    ) {}

    public record ExchangeResponse(Long id, String code, String name, String country, String currency, Instant createdAt) {}

    public record StockRequest(
            @NotBlank @Size(max = 30) String symbol,
            @NotBlank @Size(max = 150) String companyName,
            @NotBlank @Size(max = 80) String sector,
            @NotNull @DecimalMin("0.0001") BigDecimal latestPrice,
            @NotNull Long exchangeId
    ) {}

    public record StockResponse(
            Long id, String symbol, String companyName, String sector,
            BigDecimal latestPrice, Long exchangeId, String exchangeCode
    ) {}

    public record StockPriceUploadRequest(
            @NotBlank String exchangeCode,
            @NotBlank String symbol,
            @NotNull @DecimalMin("0.0001") BigDecimal price,
            @NotNull @PositiveOrZero Long volume,
            @NotNull Instant recordedAt
    ) {}

    public record StockPricePointResponse(
            Long id, String exchangeCode, String symbol,
            BigDecimal price, Long volume, Instant recordedAt
    ) {}

    public record ApiError(int status, String error, String message, String path, Instant timestamp) {}
}
