package com.nhd.processor.repository;


import com.nhd.processor.models.StockPrice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    Page<StockPrice> findByTicker(String ticker, Pageable pageable);

    Page<StockPrice> findByTickerOrderByPriceDateAsc(String ticker, Pageable pageable);

    Page<StockPrice> findByPriceDate(LocalDate date, Pageable pageable);

    Page<StockPrice> findByPriceDateAndTickerStartingWith(LocalDate date, String prefix, Pageable pageable);

    Page<StockPrice> findByPriceDateAndTickerIn(LocalDate priceDate, List<String> tickers, Pageable pageable);

    Page<StockPrice> findByTickerAndPriceDateBetweenOrderByPriceDateAsc(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<StockPrice> findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(String ticker, LocalDate date, Pageable pageable);

}
