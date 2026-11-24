package com.stockmarket.admin.repository;

import com.stockmarket.admin.domain.ExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<ExchangeEntity, Long> {
    Optional<ExchangeEntity> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
