package com.nhd.processor.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Rule context and HTTP payload; persistence is owned by nhd-stocksdata. */
@Data
@NoArgsConstructor

public class MomentumZStatsScore {

    private Integer id;

    private String tickerListName;

    private LocalDate priceDate;

    private String ticker; 

    private BigDecimal z12;

    private BigDecimal z6;

    private BigDecimal zw;

    private BigDecimal nmr;

}
