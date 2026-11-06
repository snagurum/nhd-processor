package com.nhd.processor.services;

import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.services.repository.MomentumStatsRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MomentumStatsService {
    private static final Logger log = LoggerFactory.getLogger(MomentumStatsService.class);

    private MomentumStatsRepository momentumStatsRepository;

    public void saveAll(List<MomentumStats> momentumStats){
        if (momentumStats == null || momentumStats.isEmpty()) return;
    
        List<MomentumStats> toBeMomentumStats = new ArrayList<>();
        List<LocalDate> distinctPriceDates = momentumStats.stream()
            .map(MomentumStats::getPriceDate)
            .distinct()
            .collect(Collectors.toList());
        Map<String, MomentumStats> existingMap = getExistingMomentumStats(distinctPriceDates);

        momentumStats.forEach(ss -> {
            String key = ss.getTicker() + "_" + ss.getPriceDate();
            if (existingMap.containsKey(key)) {
                toBeMomentumStats.add(updateExistingMomentumStats(existingMap.get(key), ss));
            } else {
                toBeMomentumStats.add(ss);
            }
        });
        momentumStatsRepository.saveAll(toBeMomentumStats);
    }


    public Map<String, MomentumStats> getExistingMomentumStats(List<LocalDate> priceDates){

        Map<String, MomentumStats> existingMap = new HashMap<>();
        for (LocalDate priceDate : priceDates) {            
            List<MomentumStats> momentumStats = momentumStatsRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            Map<String, MomentumStats> tempMap = momentumStats.stream()
            .collect(Collectors.toMap(
                ss -> ss.getTicker() + "_" + ss.getPriceDate(),
                ss -> ss,
                (existing, replacement) -> existing
            ));
            existingMap.putAll(tempMap);
        }
        return existingMap;
    }

    public Page<MomentumStats> findByPriceDate(LocalDate priceDate, Pageable pageable){
        return momentumStatsRepository.findByPriceDate(priceDate, pageable);
    }

    public Page<MomentumStats> getMomentumStatsForDate(LocalDate date, List<String> tickers, Pageable pageable){
        Page<MomentumStats> momentumStatsPage;
        if(null == tickers || tickers.size() == 0 || "***ALL***".equals(tickers.get(0)))
            momentumStatsPage = momentumStatsRepository.findByPriceDate(date, pageable);
        else if (tickers.size() > 1)
            momentumStatsPage = momentumStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
        else
            momentumStatsPage = momentumStatsRepository.findByPriceDateAndTickerStartingWith(date, ""+tickers.get(0).charAt(0), pageable);
        return momentumStatsPage;
    }

    public Page<MomentumStats> getByTicker(String ticker, LocalDate fromDate, Pageable pageable){
        if(fromDate == null)
            return momentumStatsRepository.findByTickerOrderByPriceDate(ticker, pageable);
        else return momentumStatsRepository.findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(ticker, fromDate, pageable);
    }

    private MomentumStats updateExistingMomentumStats(MomentumStats oldMs, MomentumStats newMs){
		if( null != newMs.getDailyReturn())             oldMs.setDailyReturn(newMs.getDailyReturn());
		if( null != newMs.getLnDailyReturn())           oldMs.setLnDailyReturn(newMs.getLnDailyReturn());

		if( null != newMs.getMean1yrLnDailyReturn())    oldMs.setMean1yrLnDailyReturn(newMs.getMean1yrLnDailyReturn());
		if( null != newMs.getSum1yrLnDailyReturn())     oldMs.setSum1yrLnDailyReturn(newMs.getSum1yrLnDailyReturn());
		if( null != newMs.getN1yr())                    oldMs.setN1yr(newMs.getN1yr());
		if( null != newMs.getStdDev1yrLnDailyReturn())  oldMs.setStdDev1yrLnDailyReturn(newMs.getStdDev1yrLnDailyReturn());
		if( null != newMs.getStdDev1yrAnnualized())     oldMs.setStdDev1yrAnnualized(newMs.getStdDev1yrAnnualized());
		if( null != newMs.getPriceReturn1yr())          oldMs.setPriceReturn1yr(newMs.getPriceReturn1yr());
		if( null != newMs.getMr12())                    oldMs.setMr12(newMs.getMr12());
		
		if( null != newMs.getMean6mLnDailyReturn())    oldMs.setMean6mLnDailyReturn(newMs.getMean6mLnDailyReturn());
		if( null != newMs.getSum6mLnDailyReturn())      oldMs.setSum1yrLnDailyReturn(newMs.getSum6mLnDailyReturn());
		if( null != newMs.getN6m())                    oldMs.setN6m(newMs.getN6m());
		if( null != newMs.getStdDev6mLnDailyReturn())  oldMs.setStdDev6mLnDailyReturn(newMs.getStdDev6mLnDailyReturn());
		if( null != newMs.getStdDev6mAnnualized())     oldMs.setStdDev6mAnnualized(newMs.getStdDev6mAnnualized());
		if( null != newMs.getPriceReturn6m())          oldMs.setPriceReturn6m(newMs.getPriceReturn6m());
		if( null != newMs.getMr6())                    oldMs.setMr6(newMs.getMr6());
        return oldMs;
    }

    public Page<MomentumStats> findByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable){
        return momentumStatsRepository.findByPriceDateAndTickerStartingWith(date, ticker, pageable);
    }

    public Page<MomentumStats> findByPriceDateAndTickerIn(LocalDate date, List<String> tickers, Pageable pageable){
        return momentumStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
    }
}
