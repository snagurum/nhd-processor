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
public class StrategyRunner {

    private Long id;
    private String username;
    private String name;
    private String ruleName;
    private String tickerListName;
    private BigDecimal initialFund;
    private BigDecimal currentFund;
    private Integer rebalanceValue;
    private String rebalancePeriod;
    private Integer noOfStocks;
    private LocalDate startDate;
    private LocalDate endDate;
}
