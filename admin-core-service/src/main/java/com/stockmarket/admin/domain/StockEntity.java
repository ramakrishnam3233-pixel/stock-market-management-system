package com.stockmarket.admin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "exchange_id"}))
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String symbol;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 80)
    private String sector;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal latestPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private ExchangeEntity exchange;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public BigDecimal getLatestPrice() { return latestPrice; }
    public void setLatestPrice(BigDecimal latestPrice) { this.latestPrice = latestPrice; }
    public ExchangeEntity getExchange() { return exchange; }
    public void setExchange(ExchangeEntity exchange) { this.exchange = exchange; }
    public Instant getCreatedAt() { return createdAt; }
}
