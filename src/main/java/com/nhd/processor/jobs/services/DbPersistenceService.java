package com.nhd.processor.jobs.services;

import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.models.MomentumZStatsScore;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;
import com.nhd.processor.services.MomentumStatsService;
import com.nhd.processor.services.MomentumZStatsService;
import com.nhd.processor.services.MomentumZStatsScoreService;
import com.nhd.processor.services.StockPriceService;
import com.nhd.processor.services.StockStatsService;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor 
public class DbPersistenceService {
    private static final Logger log = LoggerFactory.getLogger(DbPersistenceService.class);
    private final StockStatsService stockStatsService;
    private final StockPriceService stockPriceService;
    private final MomentumStatsService momentumStatsService;
    private final MomentumZStatsService momentumZStatsService;
    private final MomentumZStatsScoreService momentumZStatsScoreService;

    private Map<String,Object> cachedDBdata = new HashMap<>();
    private int flushCountStockPrice = 10000;
    private int flushCountStockStats = 20000;
    private int flushCountMomentumStats = 10000;
    private int flushCountMomentumZStats = 10000;
    private int flushCountMomentumZStatsScore = 10000;
    
    public void peristData(Boolean flushAnyways, Map<String, Object> context, String destinationTable){
        cacheData(context, destinationTable);
        flushData(flushAnyways);        
    }


    @SuppressWarnings ("unchecked")
    public void flushData(boolean flushAnyways){

        List<StockPrice> stockPricesList = (List<StockPrice>) cachedDBdata.get("stockPriceList");
        if(stockPricesList != null && (flushAnyways ||  stockPricesList.size()>flushCountStockPrice)){
            stockPriceService.saveAll(stockPricesList);
            if(stockPricesList.size()>0) log.info("saving data stockPrice count = {}, priceDate = {}", stockPricesList.size(), stockPricesList.get(0).getPriceDate());
            stockPricesList.clear();
        }

        List<StockStats> stockStatsList = (List<StockStats>) cachedDBdata.get("stockStatsList");
        if(stockStatsList != null && (flushAnyways ||  stockStatsList.size()>flushCountStockStats)){
            stockStatsService.saveAll(stockStatsList);
            if(stockStatsList.size()>0) log.info("saving data stockStats count = {}, priceDate = {}", stockStatsList.size(), stockStatsList.get(0).getPriceDate());
            stockStatsList.clear();
        }
        
        List<MomentumStats> momentumStatsList = (List<MomentumStats>) cachedDBdata.get("momentumStatsList");
        if(momentumStatsList != null && (flushAnyways ||  momentumStatsList.size()>flushCountMomentumStats)){
            momentumStatsService.saveAll(momentumStatsList);
            if(momentumStatsList.size()>0) log.info("saving data momentumStats count = {}, priceDate = {}", momentumStatsList.size(), momentumStatsList.get(0).getPriceDate());
            momentumStatsList.clear();
        }
        
        List<MomentumZStats> momentumZStatsList = (List<MomentumZStats>) cachedDBdata.get("momentumZStatsList");
        if(momentumZStatsList != null && (flushAnyways ||  momentumZStatsList.size()>flushCountMomentumZStats)){
            momentumZStatsService.saveAll(momentumZStatsList);
            if(momentumZStatsList.size()>0) log.info("saving data momentumZStats count = {}, priceDate = {}", momentumZStatsList.size(), momentumZStatsList.get(0).getPriceDate());
            momentumZStatsList.clear();
        }
        
        List<MomentumZStatsScore> momentumZStatsScoreList = (List<MomentumZStatsScore>) cachedDBdata.get("momentumZStatsScoreList");
        if(momentumZStatsScoreList != null && (flushAnyways ||  momentumZStatsScoreList.size()>flushCountMomentumZStatsScore)){
            momentumZStatsScoreService.saveAll(momentumZStatsScoreList);
            if(momentumZStatsScoreList.size()>0) log.info("saving data momentumZStatsScoreList count = {}, priceDate = {}", momentumZStatsScoreList.size(), momentumZStatsScoreList.get(0).getPriceDate());
            momentumZStatsScoreList.clear();
        }

    }


    @SuppressWarnings ("unchecked")
    public void cacheData(Map<String, Object> context, String destinationTable){
        if ("STOCK_PRICE".equals(destinationTable)) {
            List<StockPrice> incomingData = (List<StockPrice>) context.get("stockPriceList");
            if (incomingData == null || incomingData.isEmpty()) return; 
            List<StockPrice> stockPriceList = (List<StockPrice>) cachedDBdata.computeIfAbsent(
                "stockPriceList",
                k -> new ArrayList<>()
            );
            logCacheStats("STOCK_PRICE", stockPriceList, incomingData);
            if(incomingData != null)    stockPriceList.addAll(incomingData);
        }
        
        if ("STOCK_STATS".equals(destinationTable)) {
            List<StockStats> incomingData = (List<StockStats>) context.get("stockStatsList");
            if (incomingData == null || incomingData.isEmpty()) return; 
            List<StockStats> stockStatsList = (List<StockStats>) cachedDBdata.computeIfAbsent(
                "stockStatsList",
                k -> new ArrayList<>()
            );
            logCacheStats("STOCK_STATS", stockStatsList, incomingData);
            if(incomingData != null)    stockStatsList.addAll(incomingData);
        }
        
        if ("MOMENTUM_STATS".equals(destinationTable)) {
            List<MomentumStats> incomingData = (List<MomentumStats>) context.get("momentumStatsList");
            if (incomingData == null || incomingData.isEmpty()) return; 
            List<MomentumStats> momentumStatsList = (List<MomentumStats>) cachedDBdata.computeIfAbsent(
                "momentumStatsList",
                k -> new ArrayList<>()
            );
            logCacheStats("MOMENTUM_STATS", momentumStatsList, incomingData);
            if(incomingData != null)    momentumStatsList.addAll(incomingData);
        }
        
        if ("MOMENTUM_Z_STATS".equals(destinationTable)) {
            MomentumZStats incomingData = (MomentumZStats) context.get("momentumZStats");
            List<MomentumZStats> momentumZStatsList = (List<MomentumZStats>) cachedDBdata.computeIfAbsent(
                "momentumZStatsList",
                k -> new ArrayList<>()
            );
            if(incomingData != null ){
                momentumZStatsList.add(incomingData);
            }
        }
        
        if ("MOMENTUM_Z_STATS_SCORE".equals(destinationTable)) {
            List<MomentumZStatsScore> incomingData = (List<MomentumZStatsScore>) context.get("momentumZStatsScoreList");
            List<MomentumZStatsScore> momentumZStatsScoreList = (List<MomentumZStatsScore>) cachedDBdata.computeIfAbsent(
                "momentumZStatsScoreList",
                k -> new ArrayList<>()
            );
            logCacheStats("MOMENTUM_Z_STATS_SCORE", momentumZStatsScoreList, incomingData);
            if(incomingData != null)    momentumZStatsScoreList.addAll(incomingData);
        }

        context.clear();
    }

    private void logCacheStats(String destTableName, List<?> masterList, List<?> incomingList){
            int momSize = (masterList != null) ? masterList.size() : 0;
            int incSize = (incomingList != null) ? incomingList.size() : 0;
            Object object = (incomingList != null && !incomingList.isEmpty() && incomingList.get(0) != null)
                                ? incomingList.get(0)
                                : null;
            log.trace("Adding {} = {}, incoming = {}, data = {}",  destTableName, momSize, incSize, object);
    }

}

