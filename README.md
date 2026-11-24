# Stock Market Microservices Platform (Java 21 + Spring Boot + MySQL)

## Architecture Overview
This repository provides a complete multi-service Spring Boot 3.x (Java 21) architecture for managing **Users**, **Exchanges**, **Stocks**, **Timestamped Stock Prices**, **Excel Bulk Upload/Download**, and **Performance Charts**.

### Modules / Services Included
1. **api-gateway-service** (`:8080`) — Routing, JWT validation, and centralized Swagger/OpenAPI aggregation entry point.
2. **admin-core-service** (`:8081`) — Admin User CRUD, Exchange CRUD, Stock CRUD, listing stocks in exchanges, uploading timestamped stock prices (`Instant` / `LocalDateTime`), and Apache POI Excel bulk upload/download.
3. **market-analytics-service** (`:8082`) — User performance chart data & rendered chart image generation (line, OHLC/candlestick-style range, moving average/comparison series), synchronous `RestClient` calls to `admin-core-service`, and asynchronous event/price-alert processing via `@Async` + `ApplicationEventPublisher` (with RabbitMQ/Kafka-ready configuration).

### Mandatory Requirements Covered
- **Java 21 & Spring Boot 3.x & MySQL**: Records, pattern matching, `RestClient`, Spring Data JPA + Hibernate, MySQL 8 driver, Flyway/JPA DDL.
- **Microservice Intercommunication (Sync + Async)**: Synchronous HTTP calls via `RestClient` (`@CircuitBreaker`/retry-friendly) + Asynchronous processing via `@Async` executor & domain events.
- **Custom Exceptions & `@RestControllerAdvice`**: `ResourceNotFoundException`, `DuplicateResourceException`, `InvalidExcelFormatException`, `UnauthorizedOperationException` mapped to clean `ApiError` responses wrapped in `ResponseEntity`.
- **Spring Security + JWT**: Stateless JWT filter (`JwtAuthenticationFilter`), BCrypt password hashing, role-based access (`ROLE_ADMIN`, `ROLE_USER`).
- **SLF4J File Logging**: `logback-spring.xml` writing rolling logs (`logs/stock-market-service.log`) with SLF4J loggers across Controllers, Services, and Utility classes.
- **Swagger / OpenAPI 3 (`springdoc-openapi`)**: Available at `/swagger-ui.html` and `/v3/api-docs`.
- **JUnit 5 + Mockito + JaCoCo**: Comprehensive service and controller unit tests with JaCoCo coverage verification.

## Quick Start
1. Create MySQL databases:
   ```sql
   CREATE DATABASE stock_admin_db;
   CREATE DATABASE stock_analytics_db;
   ```
2. Run tests and build with Maven:
   ```bash
   mvn clean test package
   ```
3. Start services in separate terminals or via Docker/IDE.
