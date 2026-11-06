package com.nhd.processor.services;

import com.nhd.processor.models.MomentumZStatsScore;
import com.nhd.processor.services.repository.MomentumZStatsScoreRepository;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MomentumZStatsScoreService {
    private static final Logger log = LoggerFactory.getLogger(MomentumZStatsService.class);

    private MomentumZStatsScoreRepository momentumZStatsScoreRepository;

    public void saveAll(List<MomentumZStatsScore> momentumZStatsScores){
        if (momentumZStatsScores == null || momentumZStatsScores.isEmpty()) return;
    
        List<LocalDate> distinctPriceDates = momentumZStatsScores.stream()
            .filter(java.util.Objects::nonNull)
            .map(MomentumZStatsScore::getPriceDate)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
            
        Map<String, MomentumZStatsScore> existingMap = getExistingMomentumZStatsScores(distinctPriceDates);

        Map<String, MomentumZStatsScore> workingBatchMap = new HashMap<>();

        momentumZStatsScores.stream().filter(java.util.Objects::nonNull).forEach(mzss -> {
            if (mzss.getTickerListName() == null || mzss.getPriceDate() == null || mzss.getTicker() == null) {
                return;
            }

            String key = mzss.getTickerListName() + "_" + mzss.getPriceDate() + "_" + mzss.getTicker();
            
            if (workingBatchMap.containsKey(key)) {
                updateExistingMomentumZStatsScores(workingBatchMap.get(key), mzss);
            } else if (existingMap.containsKey(key)) {
                workingBatchMap.put(key, updateExistingMomentumZStatsScores(existingMap.get(key), mzss));
            } else {
                workingBatchMap.put(key, mzss);
            }
        });

        if (!workingBatchMap.isEmpty()) {
            momentumZStatsScoreRepository.saveAll(workingBatchMap.values());
        }
    }

    public Map<String, MomentumZStatsScore> getExistingMomentumZStatsScores(List<LocalDate> priceDates){
        Map<String, MomentumZStatsScore> existingMap = new HashMap<>();
        if (priceDates == null || priceDates.isEmpty()) return existingMap;

        for (LocalDate priceDate : priceDates) {            
            List<MomentumZStatsScore> momentumZStatsScores = momentumZStatsScoreRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            
            if (momentumZStatsScores != null) {
                Map<String, MomentumZStatsScore> tempMap = momentumZStatsScores.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                    mzss -> mzss.getTickerListName() + "_" + mzss.getPriceDate() + "_" + mzss.getTicker(),
                    mzss -> mzss,
                    (existing, replacement) -> existing
                ));
                existingMap.putAll(tempMap);
            }
        }
        return existingMap;
    }
    
    private MomentumZStatsScore updateExistingMomentumZStatsScores(MomentumZStatsScore oldMzss, MomentumZStatsScore newMzss){
        if( null != newMzss.getZ12())    oldMzss.setZ12 (newMzss.getZ12());
        if( null != newMzss.getZ6() )    oldMzss.setZ6  (newMzss.getZ6());
        if( null != newMzss.getZw())     oldMzss.setZw  (newMzss.getZw());
        if( null != newMzss.getNmr())    oldMzss.setNmr (newMzss.getNmr());
        return oldMzss;
    }
}