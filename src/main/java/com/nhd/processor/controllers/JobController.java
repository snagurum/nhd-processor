package com.nhd.processor.controllers;

import com.nhd.processor.services.StockPriceLoaderService;

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

    @GetMapping("/sp")
    public ResponseEntity<String> jobEngine(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) String ticker) {
        stockPriceLoaderService.processStockPriceForDate(fromDate, ticker);
        return ResponseEntity.ok("OK");
    }

}
