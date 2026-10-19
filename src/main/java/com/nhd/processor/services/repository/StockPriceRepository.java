package com.nhd.processor.services.repository;


import com.nhd.processor.models.StockPrice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    List<StockPrice> findByTicker(String ticker, Pageable pageable);

    Page<StockPrice> findByPriceDate(LocalDate date, Pageable pageable);

    Page<StockPrice> findByPriceDateAndTickerStartingWith(LocalDate date, String prefix, Pageable pageable);

    Page<StockPrice> findByPriceDateBetweenAndTicker(LocalDate startDate, LocalDate endDate, String ticker, Pageable pageable);

    Page<StockPrice> findByTickerAndPriceDateGreaterThanEqual(String ticker, LocalDate date, Pageable pageable);

}
