package com.nhd.processor.services.repository;

import com.nhd.processor.models.MomentumZStats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MomentumZStatsRepository extends JpaRepository<MomentumZStats, Long> {

    Page<MomentumZStats> findByPriceDate(LocalDate date, Pageable pageable);

    MomentumZStats findByPriceDateAndTickerListName(LocalDate date, String tickerListName);

    // Page<MomentumStats> findByTickerOrderByPriceDate(String Ticker, Pageable pageable);

    // Page<MomentumStats> findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(String Ticker, LocalDate fromDdte, Pageable pageable);

    // Page<MomentumStats> findByPriceDateAndTickerStartingWith(LocalDate date, String prefix, Pageable pageable);

    // Page<MomentumStats> findByPriceDateAndTickerIn(LocalDate priceDate, List<String> tickers, Pageable pageable);
}

