package com.nhd.processor.services;

import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.services.repository.MomentumZStatsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MomentumZStatsService {
    private static final Logger log = LoggerFactory.getLogger(MomentumZStatsService.class);

    private MomentumZStatsRepository momentumZStatsRepository;

    public void saveAll(List<MomentumZStats> momentumZStats){
        if (momentumZStats == null || momentumZStats.isEmpty()) return;
    
        // 1. Safe extraction of distinct dates
        List<LocalDate> distinctPriceDates = momentumZStats.stream()
            .filter(java.util.Objects::nonNull)
            .map(MomentumZStats::getPriceDate)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
            
        Map<String, MomentumZStats> existingMap = getExistingMomentumZStats(distinctPriceDates);

        // 2. Track updates inside this loop instance to catch duplicate batch items
        Map<String, MomentumZStats> workingBatchMap = new HashMap<>();

        momentumZStats.stream().filter(java.util.Objects::nonNull).forEach(ss -> {
            if (ss.getTickerListName() == null || ss.getPriceDate() == null) {
                return; // Guard against incomplete records
            }

            String key = ss.getTickerListName() + "_" + ss.getPriceDate();
            
            if (workingBatchMap.containsKey(key)) {
                // If it's already in our working batch from a previous item in this loop, update it in place
                updateExistingMomentumZStats(workingBatchMap.get(key), ss);
            } else if (existingMap.containsKey(key)) {
                // If it exists in the DB lookup, update it and add it to our tracking batch
                workingBatchMap.put(key, updateExistingMomentumZStats(existingMap.get(key), ss));
            } else {
                // Brand new key entirely
                workingBatchMap.put(key, ss);
            }
        });

        // 3. Persist the uniquely mapped records
        if (!workingBatchMap.isEmpty()) {
            momentumZStatsRepository.saveAll(workingBatchMap.values());
        }
    }

    public MomentumZStats getStockStatsForDate(LocalDate date, String tickerListName){
        return momentumZStatsRepository.findByPriceDateAndTickerListName(date,tickerListName  );
    }

    public Map<String, MomentumZStats> getExistingMomentumZStats(List<LocalDate> priceDates){
        Map<String, MomentumZStats> existingMap = new HashMap<>();
        if (priceDates == null || priceDates.isEmpty()) return existingMap;

        for (LocalDate priceDate : priceDates) {            
            List<MomentumZStats> momentumZStats = momentumZStatsRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            
            if (momentumZStats != null) {
                Map<String, MomentumZStats> tempMap = momentumZStats.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                    ss -> ss.getTickerListName() + "_" + ss.getPriceDate(),
                    ss -> ss,
                    (existing, replacement) -> existing
                ));
                existingMap.putAll(tempMap);
            }
        }
        return existingMap;
    }
    
    private MomentumZStats updateExistingMomentumZStats(MomentumZStats oldMzs, MomentumZStats newMzs){
        if( null != newMzs.getMeanMr12())             oldMzs.setMeanMr12(newMzs.getMeanMr12());
        if( null != newMzs.getStdDevMr12() )          oldMzs.setStdDevMr12(newMzs.getStdDevMr12());
        if( null != newMzs.getMeanMr6())              oldMzs.setMeanMr6(newMzs.getMeanMr6());
        if( null != newMzs.getStdDevMr6())            oldMzs.setStdDevMr6(newMzs.getStdDevMr6());
        return oldMzs;
    }
}