package com.stockmarket.admin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "exchanges", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class ExchangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code; // e.g., NSE, BSE, NASDAQ

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String country;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getCreatedAt() { return createdAt; }
}
