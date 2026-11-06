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
    name = "momentum_stats",
    schema = "nse",
    uniqueConstraints = {
        @UniqueConstraint(name = "ms_unique_ticker_date", columnNames = {"ticker", "price_date"})
    }
)
public class MomentumStats {

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

    @Column(name = "sum_1yr_ln_daily_return")
    private BigDecimal sum1yrLnDailyReturn;

    @Column(name = "mean_1yr_ln_daily_return")
    private BigDecimal mean1yrLnDailyReturn;

    @Column(name = "n_1yr")
    private BigDecimal n1yr;

    @Column(name = "std_dev_1yr_ln_daily_return")
    private BigDecimal stdDev1yrLnDailyReturn;

    @Column(name = "std_Dev_1yr_Annualized")
    private BigDecimal stdDev1yrAnnualized;

    @Column(name = "price_return_1yr")
    private BigDecimal priceReturn1yr;

    @Column(name = "mr12")
    private BigDecimal mr12;

    @Column(name = "sum_6m_ln_daily_return")
    private BigDecimal sum6mLnDailyReturn;

    @Column(name = "mean_6m_ln_daily_return")
    private BigDecimal mean6mLnDailyReturn;

    @Column(name = "n_6m")
    private BigDecimal n6m;

    @Column(name = "std_dev_6m_ln_daily_return")
    private BigDecimal stdDev6mLnDailyReturn;

    @Column(name = "std_dev_6m_annualized")
    private BigDecimal stdDev6mAnnualized;

    @Column(name = "price_return_6m")
    private BigDecimal priceReturn6m;

    @Column(name = "mr6")
    private BigDecimal mr6;
   
}