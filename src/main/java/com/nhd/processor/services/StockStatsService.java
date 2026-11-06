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
                toBeStockStats.add(updateExistingStockStats(existingMap.get(key), ss));
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

    public Page<StockStats> getStockStatsFromDate(LocalDate date, List<String> tickers, Pageable pageable){
        Page<StockStats> stockStatsPage;
        if(null == tickers || tickers.size() == 0 || "***ALL***".equals(tickers.get(0)))
            stockStatsPage = stockStatsRepository.findByPriceDate(date, pageable);
        else if (tickers.size() > 1)
            stockStatsPage = stockStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
        else
            stockStatsPage = stockStatsRepository.findByPriceDateAndTickerStartingWith(date, ""+tickers.get(0).charAt(0), pageable);
        return stockStatsPage;
    }

    public Page<StockStats> findByTicker(String ticker, LocalDate fromDate, Pageable pageable){
        if(fromDate == null)
            return stockStatsRepository.findByTickerOrderByPriceDateAsc(ticker, pageable);
        else return stockStatsRepository.findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(ticker, fromDate, pageable);
    }

    private StockStats updateExistingStockStats(StockStats oldSs, StockStats newSs){
		if( null != newSs.getDailyReturn())      oldSs.setDailyReturn(newSs.getDailyReturn());
		if( null != newSs.getLnDailyReturn())    oldSs.setLnDailyReturn(newSs.getLnDailyReturn());
		if( null != newSs.getDma50())            oldSs.setDma50(newSs.getDma50());
		if( null != newSs.getDma200())           oldSs.setDma200(newSs.getDma200());
        return oldSs;
    }

    public Page<StockStats> findByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable){
        return stockStatsRepository.findByPriceDateAndTickerStartingWith(date, ticker, pageable);
    }

    public Page<StockStats> findByPriceDateAndTickerIn(LocalDate date, List<String> tickers, Pageable pageable){
        return stockStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
    }
}
