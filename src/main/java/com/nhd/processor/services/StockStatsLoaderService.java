package com.nhd.processor.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service 
public class StockStatsLoaderService {
    
    private static final Logger log = LoggerFactory.getLogger(StockStatsLoaderService.class);
    private static final int limit = 2000;
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yyyy")
            .toFormatter(Locale.ENGLISH);


        
    private final RuleEngineService ruleEngineService;
    private final RuleService ruleService;
    private final StockPriceService stockPriceService;
    private final StockStatsService stockStatsService;

    public StockStatsLoaderService(
            RuleEngineService ruleEngineService,
            RuleService ruleService,
            StockPriceService stockPriceService,
            StockStatsService stockStatsService) {
        this.ruleEngineService =ruleEngineService;
        this.ruleService = ruleService;
        this.stockPriceService = stockPriceService;
        this.stockStatsService = stockStatsService;
    }


    public void processStockPriceForDate(LocalDate fromDate) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (null == fromDate) ? LocalDate.parse("01-Apr-1997", DATE_FORMATTER) : fromDate;
        List<StockPrice> accumulatedStockPrices = new ArrayList<>();
        startDate.datesUntil(endDate).forEach(currentDate -> 
            processStockPriceForDate0(currentDate, accumulatedStockPrices)
        );
        if (!accumulatedStockPrices.isEmpty()) {
            flushAccumulatedData(accumulatedStockPrices);
        }
    }

    public void processStockPriceForDate0(LocalDate date, List<StockPrice> accumulatedStockPrices) {
        List<StockPrice> stockPrices = getStockPricesForDate(date);
        
        if (stockPrices.size() == 0) {
            log.info("No Data available for stockPrices on date = {}", date);
            return;
        }
        accumulatedStockPrices.addAll(stockPrices);
        if (accumulatedStockPrices.size() > 4000) {
            flushAccumulatedData(accumulatedStockPrices);
        }
    }

    public List<StockPrice> getStockPricesForDate(LocalDate date){
        int pageNumber = 0;
        int pageSize = limit;
        Page<StockPrice> stockPricePage;
        List<StockPrice> stockPrices = new ArrayList<>();

        do {
            PageRequest pageable = PageRequest.of(pageNumber, pageSize);
            stockPricePage = stockPriceService.findByPriceDate(date, pageable);
            stockPrices.addAll(stockPricePage.getContent());
            pageNumber++;
        } while (stockPricePage.hasNext());
        return stockPrices;
    }
    
    private void flushAccumulatedData(List<StockPrice> accumulatedStockPrices) {
        List<StockStats> stockStatsList = executeRuleEngine(accumulatedStockPrices);
        stockStatsService.saveAll(stockStatsList);
        log.info("SS Loader Processed total accumulated stockprices({}) and stockstats({})", accumulatedStockPrices.size(), stockStatsList.size());
        accumulatedStockPrices.clear();
    }

    public List<StockStats> executeRuleEngine(List<StockPrice> stockPriceList){
        List<StockStats> stockStatsList = new ArrayList<>(stockPriceList.size());
        for (int i = 0; i < stockPriceList.size(); i++) {
            stockStatsList.add(new StockStats());
        }

        Map<String, Object> context = new HashMap<>();
        context.put("stockPriceList", stockPriceList);
        context.put("stockStatsList", stockStatsList);
        context.put("df", DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));

        ruleEngineService.executeRule(context, ruleService.getRulesByName("SS Loader"));
        return stockStatsList;

    }
    
}
