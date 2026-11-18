package com.nhd.processor.jobs.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.models.TickerList;
import com.nhd.processor.re.services.RuleEngineService;
import com.nhd.processor.services.RuleService;
import com.nhd.processor.services.StockService;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ETLEngineService {

    private final RuleService ruleService;
    private final RuleEngineService ruleEngineService;
    private final RuleEngineDataLoader ruleEngineDataLoaderService;
    private final DbPersistenceService dbPersistenceService;
    private final StockService stockService;
    private final ProcessorProperties properties;

    public void executeJobRunner(JobRunner jobRunner, LocalDate fromDate){
        executeJobRunner(jobRunner, null, fromDate);
    }

    public void executeJobRunner(JobRunner jobRunner, List<String> requestedTickers, LocalDate fromDate){
        log.info("Running JobRunner {}",jobRunner);
        TickerList tickerList = jobRunner.getTickerList();
        List<String> configuredTickers = (tickerList != null)
            ? List.of(tickerList.getTickerList().split(","))
            : new ArrayList<>();
        List<String> tickers = normalizeTickers(requestedTickers);
        boolean useConfiguredTickers = tickers.isEmpty();
        if (useConfiguredTickers) {
            tickers = configuredTickers;
        }
        String tickerListName = jobRunner.getTickerList().getName();
        List<String> ruleNames = List.of(jobRunner.getRules().split(","));

        for (String ruleName : ruleNames) {
            RuleDTO rule = ruleService.getRuleByName(ruleName);
            log.info("rule details  = {}", rule);
            LocalDate startDate = getStartDate(rule, fromDate);
            if("FROM_DATE".equals(rule.getAlgoType())){
                if(useConfiguredTickers && "ALL".equals(tickerListName))
                    executeRuleForFromDate(rule, startDate, null, tickerListName);
                else executeRuleForFromDate(rule, startDate, tickers, tickerListName);
            }else{
                executeRuleForTickersParallelRE(rule, tickers, fromDate, tickerListName);
            }
        }
    }

    private static List<String> normalizeTickers(List<String> tickers) {
        if (tickers == null) {
            return List.of();
        }
        return tickers.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(ticker -> !ticker.isEmpty())
            .collect(Collectors.toList());
    }

    public void executeRuleForFromDate(RuleDTO rule, LocalDate startDate , List<String> tickers, String tickerListName){
        Map<String, Object> context = new HashMap<>();
        DbPersistenceService.PersistenceBuffer buffer = dbPersistenceService.newBuffer();
        LocalDate endDate = LocalDate.now();
        Map<LocalDate, LocalDate> tradingDates = ruleEngineDataLoaderService.getDistinctTradingDates();
        startDate.datesUntil(endDate).forEach(currentDate -> {
            if(tradingDates.containsKey(currentDate)){
                ruleEngineDataLoaderService.loadSourceTableDataForFromDate(context, rule, currentDate, tickers, tickerListName);
                ruleEngineDataLoaderService.loadDestTable(context, rule, tickerListName);
                ruleEngineService.executeRule(context, rule);
                dbPersistenceService.peristData(buffer, false, context, rule.getDestinationTable());
            }
        });
        dbPersistenceService.peristData(buffer, true, context, rule.getDestinationTable());
    }

    public void executeRuleForTickers(RuleDTO rule, List<String> tickers, LocalDate startDate, String tickerListName){
        Map<String, Object> context = new HashMap<>();
        DbPersistenceService.PersistenceBuffer buffer = dbPersistenceService.newBuffer();

        if(tickers.size() == 1 && "***ALL***".equals(tickers.get(0))){
            tickers = stockService.getAllActiveTickers();
        }

        for (String ticker : tickers) {
            ruleEngineDataLoaderService.loadSourceTableDataForTicker(context, rule, ticker, startDate, tickerListName);
            ruleEngineDataLoaderService.loadDestTable(context, rule, tickerListName);
            ruleEngineService.executeRule(context, rule);
            dbPersistenceService.peristData(buffer, false, context, rule.getDestinationTable());
        }
        dbPersistenceService.peristData(buffer, true, context, rule.getDestinationTable());
    }

    public void executeRuleForTickersParallelRE(RuleDTO rule, List<String> tickers, LocalDate startDate, String tickerListName) {
        DbPersistenceService.PersistenceBuffer buffer = dbPersistenceService.newBuffer();
        if (tickers.size() == 1 && "***ALL***".equals(tickers.get(0))) {
            tickers = stockService.getAllActiveTickers();
        }

        int chunkSize = properties.getEtlTickerChunkSize();

        for (int i = 0; i < tickers.size(); i += chunkSize) {
            List<String> chunk = tickers.subList(i, Math.min(i + chunkSize, tickers.size()));
            log.info("loading ruleEngine data for tickers[{}..{}]({})...", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            List<Map<String,Object>> allContexts = ruleEngineDataLoaderService.loadSourceDestDataForTickers(rule, chunk, startDate);
            log.info("Executing ruleEngine for tickers [{}..{}]({})", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            allContexts.parallelStream()
                .forEach(localContext -> {
                    ruleEngineService.executeRule(localContext, rule);
                });
            log.info("persisting data for tickers[{}..{}]({})", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            dbPersistenceService.peristData(buffer, true, allContexts, rule.getDestinationTable());
        }
    }

    private LocalDate getStartDate(RuleDTO rule, LocalDate fromDate){
        LocalDate startDate = (null == fromDate) ? properties.getEtlDefaultFromDate() : fromDate;
        if(rule.getType().equals("MOMENTUM"))
            startDate = (null == fromDate) ? properties.getEtlMomentumFromDate() : fromDate;
        return startDate;
    }
}
