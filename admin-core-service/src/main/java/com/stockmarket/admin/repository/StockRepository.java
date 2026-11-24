package com.stockmarket.admin.repository;

import com.stockmarket.admin.domain.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, Long> {
    List<StockEntity> findByExchangeId(Long exchangeId);
    Optional<StockEntity> findBySymbolIgnoreCaseAndExchange_CodeIgnoreCase(String symbol, String exchangeCode);
    boolean existsBySymbolIgnoreCaseAndExchangeId(String symbol, Long exchangeId);
}
