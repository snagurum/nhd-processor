package com.nhd.processor.services;

import com.nhd.processor.models.StockStats;
import com.nhd.processor.services.repository.StockStatsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StockStatsService {
    private static final Logger log = LoggerFactory.getLogger(StockStatsService.class);

    private StockStatsRepository stockStatsRepository;

    public void saveAll(List<StockStats> stockStats){
        if (stockStats == null || stockStats.isEmpty()) return;
    
        List<StockStats> toBeStockStats = new ArrayList<>();
        List<LocalDate> distinctPriceDates = stockStats.stream()
            .map(StockStats::getPriceDate)
            .distinct()
            .collect(Collectors.toList());
        Map<String, StockStats> existingMap = getExistingStockStats(distinctPriceDates);

        stockStats.forEach(ss -> {
            String key = ss.getTicker() + "_" + ss.getPriceDate();
            if (existingMap.containsKey(key)) {
                StockStats existingRecord = existingMap.get(key);
                existingRecord.setDailyReturn(ss.getDailyReturn());
                existingRecord.setLnDailyReturn(ss.getLnDailyReturn());
                toBeStockStats.add(updateExistingStockPrice(existingMap.get(key), ss));
            } else {
                toBeStockStats.add(ss);
            }
        });
        stockStatsRepository.saveAll(toBeStockStats);
    }


    public Map<String, StockStats> getExistingStockStats(List<LocalDate> priceDates){

        Map<String, StockStats> existingMap = new HashMap<>();
        for (LocalDate priceDate : priceDates) {            
            List<StockStats> stockStats = stockStatsRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            Map<String, StockStats> tempMap = stockStats.stream()
            .collect(Collectors.toMap(
                ss -> ss.getTicker() + "_" + ss.getPriceDate(),
                ss -> ss,
                (existing, replacement) -> existing
            ));
            existingMap.putAll(tempMap);
        }
        return existingMap;
    }

    public Page<StockStats> findByPriceDate(LocalDate priceDate, Pageable pageable){
        return stockStatsRepository.findByPriceDate(priceDate, pageable);
    }

    private StockStats updateExistingStockPrice(StockStats oldSs, StockStats newSs){
        oldSs.setDailyReturn(newSs.getDailyReturn());
        oldSs.setLnDailyReturn(newSs.getLnDailyReturn());
        return oldSs;
    }
}
