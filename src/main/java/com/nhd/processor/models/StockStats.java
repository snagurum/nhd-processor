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

    @Column(name = "return_1yr")
    private BigDecimal return1yr;

    @Column(name = "return_6m")
    private BigDecimal return6m;

    @Column(name = "return_3m")
    private BigDecimal return3m;

    @Column(name = "return_1m")
    private BigDecimal return1m;

    @Column(name = "std_dev_1yr")
    private BigDecimal stdDev1yr;

    @Column(name = "std_dev_6m")
    private BigDecimal stdDev6m;

    @Column(name = "std_dev_3m")
    private BigDecimal stdDev3m;

    @Column(name = "std_dev_1m")
    private BigDecimal stdDev1m;
    
}