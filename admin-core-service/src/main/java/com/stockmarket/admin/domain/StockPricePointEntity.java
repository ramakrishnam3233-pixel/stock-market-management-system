package com.stockmarket.admin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_price_points", indexes = {
    @Index(name = "idx_stock_price_recorded_at", columnList = "stock_id, recorded_at")
})
public class StockPricePointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private StockEntity stock;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Long volume;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StockEntity getStock() { return stock; }
    public void setStock(StockEntity stock) { this.stock = stock; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
