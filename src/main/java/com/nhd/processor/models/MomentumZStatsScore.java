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
    name = "momentum_z_stats_score",
    schema = "nse",
    uniqueConstraints = {
        @UniqueConstraint(name = "mzss_unique_tickerlist_date_ticker", columnNames = {"ticker_list_name", "price_date", "ticker"})
    }
)
public class MomentumZStatsScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "ticker_list_name", nullable = false, length = 20)
    private String tickerListName;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker; 

    @Column(name = "z12")
    private BigDecimal z12;

    @Column(name = "z6")
    private BigDecimal z6;

    @Column(name = "zw")
    private BigDecimal zw;

    @Column(name = "nmr")
    private BigDecimal nmr;

}
