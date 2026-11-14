package com.nhd.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
@Component
@ConfigurationProperties(prefix = "processor")
public class ProcessorProperties {

    private int paginationPageSize = 2000;
    private int apiStockStatsPageSize = 1000;
    private int etlTickerChunkSize = 250;
    private LocalDate etlDefaultFromDate = LocalDate.of(1996, 1, 1);
    private LocalDate etlMomentumFromDate = LocalDate.of(2014, 1, 1);
    private int persistenceJdbcBatchSize = 10000;
    private int persistenceFlushStockPrice = 10000;
    private int persistenceFlushStockStats = 500000;
    private int persistenceFlushMomentumStats = 10000;
    private int persistenceFlushMomentumZStats = 10000;
    private int persistenceFlushMomentumZStatsScore = 10000;
    private Duration loaderClientConnectTimeout = Duration.ofSeconds(10);
    private Duration loaderClientReadTimeout = Duration.ofSeconds(60);

}
