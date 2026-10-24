package com.nhd.processor.services.repository;

import com.nhd.processor.models.StockStats;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockStatsRepository extends JpaRepository<StockStats, Long> {
      Page<StockStats> findByPriceDate(LocalDate date, Pageable pageable);
}
