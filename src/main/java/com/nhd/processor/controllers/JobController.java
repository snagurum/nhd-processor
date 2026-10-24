package com.nhd.processor.controllers;

import com.nhd.processor.services.StockPriceLoaderService;
import com.nhd.processor.services.StockStatsLoaderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/runJob")
@AllArgsConstructor
public class JobController {

    private StockPriceLoaderService stockPriceLoaderService;
    private StockStatsLoaderService stockStatsLoaderService;

    @GetMapping("/sp")
    public ResponseEntity<String> jobEngineStockPrice(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) String ticker) {
        stockPriceLoaderService.processStockPriceForDate(fromDate, ticker);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/ss")
    public ResponseEntity<String> jobEngineStockStats(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) String ticker) {
        stockStatsLoaderService.processStockPriceForDate(fromDate);
        return ResponseEntity.ok("OK");
    }

}
