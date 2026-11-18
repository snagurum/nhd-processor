package com.nhd.processor.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockStats {

    private Integer id;

    private String ticker;

    private LocalDate priceDate;

    private BigDecimal dailyReturn;

    private BigDecimal lnDailyReturn;

    private BigDecimal dma50;

    private BigDecimal dma200;

    private BigDecimal return1yr;

    private BigDecimal return6m;

    private BigDecimal return3m;

    private BigDecimal return1m;

    private BigDecimal stdDev1yr;

    private BigDecimal stdDev6m;

    private BigDecimal stdDev3m;

    private BigDecimal stdDev1m;

    private BigDecimal rsi14;

    private BigDecimal rsi7;
    private BigDecimal return2Volatility;

    private BigDecimal dropPercentage;
}
