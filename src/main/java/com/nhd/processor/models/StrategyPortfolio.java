package com.nhd.processor.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyPortfolio {

    private Long id;
    private String stockName;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private LocalDate rebalanceDate;
    private LocalDate executionDate;
    private Long strategyRunnerId;
}
