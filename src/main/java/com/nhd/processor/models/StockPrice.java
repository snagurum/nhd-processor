package com.nhd.processor.models;


import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(
    name = "stock_price",
    schema = "nse",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_ticker_date", columnNames = {"ticker", "price_date"})
    }
)
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "series", length = 10)
    private String series;

    @Column(name = "open", precision = 10, scale = 2)
    private BigDecimal open;

    @Column(name = "high", precision = 10, scale = 2)
    private BigDecimal high;

    @Column(name = "low", precision = 10, scale = 2)
    private BigDecimal low;

    @Column(name = "prev_close", precision = 10, scale = 2)
    private BigDecimal prevClose;

    @Column(name = "close", precision = 10, scale = 2)
    private BigDecimal close;

    @Column(name = "vwap", precision = 10, scale = 2)
    private BigDecimal vwap;

    @Column(name = "high52", precision = 10, scale = 2)
    private BigDecimal high52;

    @Column(name = "low52", precision = 10, scale = 2)
    private BigDecimal low52;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;

    @Column(name = "no_of_trades")
    private Long noOfTrades;

    @Column(name = "ltp", precision = 10, scale = 2)
    private BigDecimal ltp;

    @Column(name = "ffmc")
    private Long ffmc;
}
