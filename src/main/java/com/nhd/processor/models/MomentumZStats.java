package com.nhd.processor.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Rule context and HTTP payload; persistence is owned by nhd-stocksdata. */
@Data
@NoArgsConstructor

public class MomentumZStats {

    private Integer id;

    private String tickerListName;

    private LocalDate priceDate;   

    private BigDecimal meanMr12;

    private BigDecimal stdDevMr12;

    private BigDecimal meanMr6;

    private BigDecimal stdDevMr6;

}
