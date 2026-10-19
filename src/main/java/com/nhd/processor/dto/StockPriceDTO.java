package com.nhd.processor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class StockPriceDTO {

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