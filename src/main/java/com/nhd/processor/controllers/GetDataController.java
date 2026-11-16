package com.nhd.processor.controllers;

import com.nhd.processor.jobs.services.ETLEngineService;
import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;
import com.nhd.processor.services.JobRunnerService;
import com.nhd.processor.services.StockStatsService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class GetDataController {
	private static final Logger log = LoggerFactory.getLogger(GetDataController.class);

    private final StockStatsService stockStatsService;
    private final ProcessorProperties properties;

    @GetMapping("/stockStats")
    public ResponseEntity<List<StockStats>> getStockStats(
        @RequestParam LocalDate fromDate,
        @RequestParam(required = false) List<String> tickers
    ) {
        int pageNumber = 0, pageSize = properties.getApiStockStatsPageSize();
        Page<StockStats> stockStatsPage;
        List<StockStats> stockStats = new ArrayList<>();
        try {
            do {
                PageRequest pageable = PageRequest.of(pageNumber, pageSize);
                stockStatsPage = stockStatsService.getStockStatsFromDate(fromDate, tickers, pageable);
                stockStats.addAll(stockStatsPage.getContent());
                pageNumber++;
            } while (stockStatsPage.hasNext());
        } catch (Exception e) {
            log.error("Error Occured... ",e);
        }
        return ResponseEntity.ok(stockStats);
    }

}
