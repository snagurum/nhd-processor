package com.nhd.processor.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Rule context and HTTP payload; persistence is owned by nhd-stocksdata. */
@Data
@NoArgsConstructor

public class StockPrice {

    private Integer id;

    private String ticker;

    private LocalDate priceDate;

    private String series;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal prevClose;

    private BigDecimal close;

    private BigDecimal vwap;

    private BigDecimal high52;

    private BigDecimal low52;

    private Long volume;

    private BigDecimal value;

    private Long noOfTrades;

    private BigDecimal ltp;

    private Long ffmc;
}
