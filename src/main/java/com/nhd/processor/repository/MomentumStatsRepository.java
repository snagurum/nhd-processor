package com.nhd.processor.repository;

import com.nhd.processor.models.MomentumStats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface MomentumStatsRepository extends JpaRepository<MomentumStats, Long> {

    Page<MomentumStats> findByPriceDate(LocalDate date, Pageable pageable);

    Page<MomentumStats> findByTickerOrderByPriceDate(String Ticker, Pageable pageable);

    Page<MomentumStats> findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(String Ticker, LocalDate fromDdte, Pageable pageable);

    Page<MomentumStats> findByPriceDateAndTickerStartingWith(LocalDate date, String prefix, Pageable pageable);

    Page<MomentumStats> findByPriceDateAndTickerIn(LocalDate priceDate, List<String> tickers, Pageable pageable);
}

