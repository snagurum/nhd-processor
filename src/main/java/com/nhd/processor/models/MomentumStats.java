package com.nhd.processor.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Rule context and HTTP payload; persistence is owned by nhd-stocksdata. */
@Data
@NoArgsConstructor

public class MomentumStats {

    private Integer id;

    private String ticker;

    private LocalDate priceDate;

    private BigDecimal dailyReturn;

    private BigDecimal lnDailyReturn;

    private BigDecimal sum1yrLnDailyReturn;

    private BigDecimal mean1yrLnDailyReturn;

    private BigDecimal n1yr;

    private BigDecimal stdDev1yrLnDailyReturn;

    private BigDecimal stdDev1yrAnnualized;

    private BigDecimal priceReturn1yr;

    private BigDecimal mr12;

    private BigDecimal sum6mLnDailyReturn;

    private BigDecimal mean6mLnDailyReturn;

    private BigDecimal n6m;

    private BigDecimal stdDev6mLnDailyReturn;

    private BigDecimal stdDev6mAnnualized;

    private BigDecimal priceReturn6m;

    private BigDecimal mr6;
   
}