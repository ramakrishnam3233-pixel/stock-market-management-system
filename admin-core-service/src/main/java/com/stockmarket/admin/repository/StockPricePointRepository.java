package com.stockmarket.admin.repository;

import com.stockmarket.admin.domain.StockPricePointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface StockPricePointRepository extends JpaRepository<StockPricePointEntity, Long> {
    List<StockPricePointEntity> findByStockIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long stockId, Instant from, Instant to);
    List<StockPricePointEntity> findByStockIdOrderByRecordedAtAsc(Long stockId);
}
