package com.nhd.processor.jobs.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.models.MomentumZStatsScore;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;
import com.nhd.processor.models.StrategyPortfolio;
import com.nhd.processor.models.StrategyTrade;
import com.nhd.processor.services.MomentumStatsService;
import com.nhd.processor.services.PortfolioService;
import com.nhd.processor.services.StockPriceService;
import com.nhd.processor.services.StockStatsService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbPersistenceService {
    private final StockStatsService stockStatsService;
    private final StockPriceService stockPriceService;
    private final MomentumStatsService momentumStatsService;
    private final PortfolioService portfolioService;
    private final ProcessorProperties properties;

    public PersistenceBuffer newBuffer(){
        return new PersistenceBuffer();
    }

    public void peristData(PersistenceBuffer buffer, Boolean flushAnyways, List<Map<String, Object>> allContexts, String destinationTable){
        for (Map<String,Object> context : allContexts) {
            cacheData(buffer, context, destinationTable);
            flushData(buffer, false);
        }
        flushData(buffer, true);
    }

    public void peristData(PersistenceBuffer buffer, Boolean flushAnyways, Map<String, Object> context, String destinationTable){
        cacheData(buffer, context, destinationTable);
        flushData(buffer, flushAnyways);
    }

    @SuppressWarnings ("unchecked")
    public void flushData(PersistenceBuffer buffer, boolean flushAnyways){

        List<StockPrice> stockPricesList = (List<StockPrice>) buffer.cachedDBdata.get("stockPriceList");
        if(stockPricesList != null && (flushAnyways || stockPricesList.size() > properties.getPersistenceFlushStockPrice())){
            stockPriceService.saveAll(stockPricesList);
            stockPricesList.clear();
        }

        List<StockStats> stockStatsList = (List<StockStats>) buffer.cachedDBdata.get("stockStatsList");
        if(stockStatsList != null && (flushAnyways || stockStatsList.size() > properties.getPersistenceFlushStockStats())){
            stockStatsService.saveAll(stockStatsList);
            stockStatsList.clear();
        }
        
        List<MomentumStats> momentumStatsList = (List<MomentumStats>) buffer.cachedDBdata.get("momentumStatsList");
        if(momentumStatsList != null && (flushAnyways || momentumStatsList.size() > properties.getPersistenceFlushMomentumStats())){
            momentumStatsService.saveMomentumStats(momentumStatsList);
            momentumStatsList.clear();
        }
        
        List<MomentumZStats> momentumZStatsList = (List<MomentumZStats>) buffer.cachedDBdata.get("momentumZStatsList");
        if(momentumZStatsList != null && (flushAnyways || momentumZStatsList.size() > properties.getPersistenceFlushMomentumZStats())){
            momentumStatsService.saveMomentumZStats(momentumZStatsList);
            momentumZStatsList.clear();
        }
        
        List<MomentumZStatsScore> momentumZStatsScoreList = (List<MomentumZStatsScore>) buffer.cachedDBdata.get("momentumZStatsScoreList");
        if(momentumZStatsScoreList != null && (flushAnyways || momentumZStatsScoreList.size() > properties.getPersistenceFlushMomentumZStatsScore())){
            momentumStatsService.saveMomentumZStatsScores(momentumZStatsScoreList);
            if(momentumZStatsScoreList.size()>0) log.info("saving data momentumZStatsScoreList count = {}, priceDate = {}", momentumZStatsScoreList.size(), momentumZStatsScoreList.get(0).getPriceDate());
            momentumZStatsScoreList.clear();
        }

        Deque<StrategyPortfolio> strategyPortfolios = (Deque<StrategyPortfolio>) buffer.cachedDBdata.get("strategyPortfolioList");
        if (strategyPortfolios != null && (flushAnyways || strategyPortfolios.size() > properties.getPersistenceFlushStrategyPortfolio())) {
            while (!strategyPortfolios.isEmpty()) {
                portfolioService.savePortfolio(strategyPortfolios.getFirst());
                strategyPortfolios.removeFirst();
            }
        }

        Deque<StrategyTrade> strategyTrades = (Deque<StrategyTrade>) buffer.cachedDBdata.get("strategyTradeList");
        if (strategyTrades != null && (flushAnyways || strategyTrades.size() > properties.getPersistenceFlushStrategyTrade())) {
            while (!strategyTrades.isEmpty()) {
                portfolioService.saveTrade(strategyTrades.getFirst());
                strategyTrades.removeFirst();
            }
        }

    }

    @SuppressWarnings ("unchecked")
    public void cacheData(PersistenceBuffer buffer, Map<String, Object> context, String destinationTables) {
        List<String> destinations = destinationTables == null
            ? List.of()
            : List.of(destinationTables.split(",")).stream()
                .map(String::trim)
                .filter(destination -> !destination.isEmpty())
                .distinct()
                .toList();

        boolean clearContext = destinations.isEmpty();
        for (String destinationTable : destinations) {
            if ("STOCK_PRICE".equals(destinationTable)) {
                List<StockPrice> incomingData = (List<StockPrice>) context.get("stockPriceList");
                if (incomingData == null || incomingData.isEmpty()) continue;
                List<StockPrice> stockPriceList = (List<StockPrice>) buffer.cachedDBdata.computeIfAbsent(
                    "stockPriceList",
                    k -> new ArrayList<>()
                );
                logCacheStats("STOCK_PRICE", stockPriceList, incomingData);
                if(incomingData != null)    stockPriceList.addAll(incomingData);
            }

            if ("STOCK_STATS".equals(destinationTable)) {
                List<StockStats> incomingData = (List<StockStats>) context.get("stockStatsList");
                if (incomingData == null || incomingData.isEmpty()) continue;
                List<StockStats> stockStatsList = (List<StockStats>) buffer.cachedDBdata.computeIfAbsent(
                    "stockStatsList",
                    k -> new ArrayList<>()
                );
                logCacheStats("STOCK_STATS", stockStatsList, incomingData);
                if(incomingData != null)    stockStatsList.addAll(incomingData);
            }

            if ("MOMENTUM_STATS".equals(destinationTable)) {
                List<MomentumStats> incomingData = (List<MomentumStats>) context.get("momentumStatsList");
                if (incomingData == null || incomingData.isEmpty()) continue;
                List<MomentumStats> momentumStatsList = (List<MomentumStats>) buffer.cachedDBdata.computeIfAbsent(
                    "momentumStatsList",
                    k -> new ArrayList<>()
                );
                logCacheStats("MOMENTUM_STATS", momentumStatsList, incomingData);
                if(incomingData != null)    momentumStatsList.addAll(incomingData);
            }

            if ("MOMENTUM_Z_STATS".equals(destinationTable)) {
                MomentumZStats incomingData = (MomentumZStats) context.get("momentumZStats");
                List<MomentumZStats> momentumZStatsList = (List<MomentumZStats>) buffer.cachedDBdata.computeIfAbsent(
                    "momentumZStatsList",
                    k -> new ArrayList<>()
                );
                if(incomingData != null ){
                    momentumZStatsList.add(incomingData);
                }
            }

            if ("MOMENTUM_Z_STATS_SCORE".equals(destinationTable)) {
                List<MomentumZStatsScore> incomingData = (List<MomentumZStatsScore>) context.get("momentumZStatsScoreList");
                List<MomentumZStatsScore> momentumZStatsScoreList = (List<MomentumZStatsScore>) buffer.cachedDBdata.computeIfAbsent(
                    "momentumZStatsScoreList",
                    k -> new ArrayList<>()
                );
                logCacheStats("MOMENTUM_Z_STATS_SCORE", momentumZStatsScoreList, incomingData);
                if(incomingData != null)    momentumZStatsScoreList.addAll(incomingData);
            }

            if ("STRATEGY_PORTFOLIO".equals(destinationTable)) {
                List<StrategyPortfolio> incomingData = (List<StrategyPortfolio>) context.get("strategyPortfolioList");
                if (incomingData == null || incomingData.isEmpty()) continue;
                Deque<StrategyPortfolio> strategyPortfolios = (Deque<StrategyPortfolio>) buffer.cachedDBdata.computeIfAbsent(
                    "strategyPortfolioList",
                    key -> new ArrayDeque<StrategyPortfolio>()
                );
                strategyPortfolios.addAll(incomingData);
            }

            if ("STRATEGY_TRADE".equals(destinationTable)) {
                List<StrategyTrade> incomingData = (List<StrategyTrade>) context.get("strategyTradeList");
                if (incomingData == null || incomingData.isEmpty()) continue;
                Deque<StrategyTrade> strategyTrades = (Deque<StrategyTrade>) buffer.cachedDBdata.computeIfAbsent(
                    "strategyTradeList",
                    key -> new ArrayDeque<StrategyTrade>()
                );
                strategyTrades.addAll(incomingData);
            }

            clearContext = true;
        }

        if (clearContext) {
            context.clear();
        }
    }

    private void logCacheStats(String destTableName, List<?> masterList, List<?> incomingList){
            int momSize = (masterList != null) ? masterList.size() : 0;
            int incSize = (incomingList != null) ? incomingList.size() : 0;
            Object object = (incomingList != null && !incomingList.isEmpty() && incomingList.get(0) != null)
                                ? incomingList.get(0)
                                : null;
            log.trace("Adding {} = {}, incoming = {}, data = {}",  destTableName, momSize, incSize, object);
    }

    public static final class PersistenceBuffer {
        private final Map<String, Object> cachedDBdata = new HashMap<>();
    }

}

