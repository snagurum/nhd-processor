package com.nhd.processor.jobs.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.models.TickerList;
import com.nhd.processor.services.RuleEngineService;
import com.nhd.processor.services.RuleService;
import com.nhd.processor.services.StockService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class ETLEngineService {

    private static final Logger log = LoggerFactory.getLogger(ETLEngineService.class);
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("dd-MMM-yyyy")
        .toFormatter(Locale.ENGLISH);
    private static LocalDate ALL_FROM_DATE = LocalDate.parse("01-Jan-1996", DATE_FORMATTER);
    private static LocalDate MOMENTUM_FROM_DATE = LocalDate.parse("01-Jan-2014", DATE_FORMATTER);

    private final RuleService ruleService;
    private final RuleEngineService ruleEngineService;
    private final RuleEngineDataLoader ruleEngineDataLoaderService;
    private final DbPersistenceService dbPersistenceService;
    private final StockService stockService;

    public void executeJobRunner(JobRunner jobRunner, LocalDate fromDate){
        log.info("Running JobRunner {}",jobRunner);
        TickerList tickerList = jobRunner.getTickerList();
        List<String> tickers = (tickerList != null)?List.of(tickerList.getTickerList().split(",")): new ArrayList<>();
        String tickerListName = jobRunner.getTickerList().getName();
        List<String> ruleNames = List.of(jobRunner.getRules().split(","));

        for (String ruleName : ruleNames) {
            RuleDTO rule = ruleService.getRuleByName(ruleName);
            log.info("rule details  = {}", rule);
            LocalDate startDate = getStartDate(rule, fromDate);
            if("FROM_DATE".equals(rule.getAlgoType())){
                if("ALL".equals(tickerListName))
                    executeRuleForFromDate(rule, startDate, null, tickerListName);
                else executeRuleForFromDate(rule, startDate, tickers, tickerListName);
            }else{
                executeRuleForTickersParallelRE(rule, tickers, fromDate, tickerListName);
            }
        }
    }

    public void executeRuleForFromDate(RuleDTO rule, LocalDate startDate , List<String> tickers, String tickerListName){
        Map<String, Object> context = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        Map<LocalDate, LocalDate> tradingDates = ruleEngineDataLoaderService.getDistinctTradingDates();
        startDate.datesUntil(endDate).forEach(currentDate -> {
            if(tradingDates.containsKey(currentDate)){
                ruleEngineDataLoaderService.loadSourceTableDataForFromDate(context, rule, currentDate, tickers, tickerListName);
                ruleEngineDataLoaderService.loadDestTable(context, rule, tickerListName);
                ruleEngineService.executeRule(context, rule);
                dbPersistenceService.peristData(false, context, rule.getDestinationTable());
            }
        });
        dbPersistenceService.peristData(true, context, rule.getDestinationTable());
    }

    public void executeRuleForTickers(RuleDTO rule, List<String> tickers, LocalDate startDate, String tickerListName){
        Map<String, Object> context = new HashMap<>();

        if("All".equalsIgnoreCase(tickerListName)){
            tickers = stockService.getAllActiveTickers();
        }

        for (String ticker : tickers) {
            ruleEngineDataLoaderService.loadSourceTableDataForTicker(context, rule, ticker, startDate, tickerListName);
            ruleEngineDataLoaderService.loadDestTable(context, rule, tickerListName);
            ruleEngineService.executeRule(context, rule);
            dbPersistenceService.peristData(false, context, rule.getDestinationTable());
        }
        dbPersistenceService.peristData(true, context, rule.getDestinationTable());
    }

    public void executeRuleForTickersParallelRE(RuleDTO rule, List<String> tickers, LocalDate startDate, String tickerListName) {
        if ("All".equalsIgnoreCase(tickerListName)) {
            tickers = stockService.getAllActiveTickers();
        }

        int chunkSize = 250;

        for (int i = 0; i < tickers.size(); i += chunkSize) {
            List<String> chunk = tickers.subList(i, Math.min(i + chunkSize, tickers.size()));
            log.info("loading ruleEngine data for tickers[{}..{}]({})...", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            List<Map<String,Object>> allContexts = ruleEngineDataLoaderService.loadSourceDestDataForTickers(rule, chunk, startDate);
            log.info("Executing ruleEngine for tickers [{}..{}]({})", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            allContexts.parallelStream()
                .forEach(localContext -> {
                    ruleEngineService.executeRule(localContext, rule);
                });
            log.info("persisting data for tickers[{}..{}]()", chunk.get(0),chunk.get(chunk.size()-1),chunk.size());
            dbPersistenceService.peristData(true, allContexts, rule.getDestinationTable());
        }
    }

    private static LocalDate getStartDate(RuleDTO rule, LocalDate fromDate){
        LocalDate startDate = (null == fromDate) ? ALL_FROM_DATE : fromDate;
        if(rule.getType().equals("MOMENTUM"))
            startDate = (null == fromDate) ? MOMENTUM_FROM_DATE : fromDate;
        return startDate;
    }
}
