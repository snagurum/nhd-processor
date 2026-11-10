package com.nhd.processor.repository;

import com.nhd.processor.models.StockStats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface StockStatsRepository extends JpaRepository<StockStats, Long> {

    Page<StockStats> findByPriceDate(LocalDate date, Pageable pageable);

    Page<StockStats> findByTickerOrderByPriceDateAsc(String Ticker, Pageable pageable);

    Page<StockStats> findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(String Ticker, LocalDate fromDdte, Pageable pageable);

    Page<StockStats> findByPriceDateAndTickerStartingWith(LocalDate date, String prefix, Pageable pageable);

    Page<StockStats> findByPriceDateAndTickerIn(LocalDate priceDate, List<String> tickers, Pageable pageable);

}
