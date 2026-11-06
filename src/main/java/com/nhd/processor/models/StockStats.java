package com.nhd.processor.models;


import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(
    name = "stock_stats",
    schema = "nse",
    uniqueConstraints = {
        @UniqueConstraint(name = "ss_unique_ticker_date", columnNames = {"ticker", "price_date"})
    }
)
public class StockStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "daily_return")
    private BigDecimal dailyReturn;

    @Column(name = "ln_daily_return")
    private BigDecimal lnDailyReturn;

    @Column(name = "dma50")
    private BigDecimal dma50;

    @Column(name = "dma200")
    private BigDecimal dma200;
    
}