package com.nhd.processor.jobs.services;

import com.nhd.processor.dto.BspSearchResponse;
import com.nhd.processor.dto.LoadBspTickerDTO;
import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.models.MomentumZStatsScore;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;
import com.nhd.processor.services.MomentumStatsService;
import com.nhd.processor.services.MomentumZStatsService;
import com.nhd.processor.services.StockPriceService;
import com.nhd.processor.services.StockStatsService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RuleEngineDataLoader {
    private static final Logger log = LoggerFactory.getLogger(RuleEngineDataLoader.class);

    private static DateTimeFormatter DF_DDMMMYYYY = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    private static final int LIMIT = 2000;

    private final String loaderUrl;
    private final RestTemplate restTemplate;
    private final StockPriceService stockPriceService;
    private final StockStatsService stockStatsService;
    private final MomentumStatsService momentumStatsService;
    private final MomentumZStatsService momentumZStatsService;
    private final ObjectMapper objectMapper;

    public RuleEngineDataLoader(@Value("${app.loader.url}") String loaderUrl
        , RestTemplate restTemplate
        , StockPriceService stockPriceService
        , StockStatsService stockStatsService
        , MomentumStatsService momentumStatsService
        , MomentumZStatsService momentumZStatsService
        , ObjectMapper objectMapper){
        this.loaderUrl = loaderUrl;
        this.restTemplate = restTemplate;
        this.stockPriceService = stockPriceService;
        this.objectMapper = objectMapper;
        this.stockStatsService = stockStatsService;
        this.momentumStatsService = momentumStatsService;
        this.momentumZStatsService = momentumZStatsService;
    }

    public Map<LocalDate, LocalDate> getDistinctTradingDates(){
        String baseUrl = this.loaderUrl + "/bsp/tradingDates";
        ParameterizedTypeReference<List<LocalDate>> typeRef = new ParameterizedTypeReference<List<LocalDate>>() {};
        ResponseEntity<List<LocalDate>> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.GET,
            null,
            typeRef
        );

        return response.getBody().stream()
            .collect(Collectors.toMap(
                    date -> date,
                    date -> date,
                    (existing, replacement) -> existing
            ));
    }

    @SuppressWarnings("unchecked")
    public void loadSourceTableDataForFromDate(Map<String, Object> context, RuleDTO rule, LocalDate startDate, List<String> tickers, String tickerListName) {
        if (rule.getSourceTables() == null) return;
        String[] sourceTables = rule.getSourceTables().split(",");

        for (String sourceTable : sourceTables) {
            switch (sourceTable.trim()) {
                case "LOAD_BSP_TICKER" -> {
                    List<LoadBspTickerDTO> existing = (List<LoadBspTickerDTO>) context.computeIfAbsent("bspList", k -> new ArrayList<>());
                    existing.addAll(getLoaderBsp(startDate, tickers));
                }
                case "df" -> context.put("df", DF_DDMMMYYYY);
                case "STOCK_PRICE" -> {
                    List<StockPrice> existing = (List<StockPrice>) context.computeIfAbsent("stockPriceList", k -> new ArrayList<>());
                    existing.addAll(stockPriceService.getStockPricesFromDate(startDate, tickers));
                }
                case "STOCK_STATS" -> {
                    List<StockStats> existing = (List<StockStats>) context.computeIfAbsent("stockStatsList", k -> new ArrayList<>());
                    existing.addAll(stockStatsService.getStockStatsFromDate(startDate, tickers));
                }
                case "MOMENTUM_STATS" -> {
                    List<MomentumStats> existingMomentumStats = (List<MomentumStats>) context.computeIfAbsent("momentumStatsList", k -> new ArrayList<>());
                    existingMomentumStats.addAll(getMomentumStatsForDate(startDate,tickers));
                    context.put("tickerListName", tickerListName);
                    context.put("priceDate", startDate);
                }
                case "MOMENTUM_Z_STATS" -> {
                    context.put("momentumZStats",getMomentumZStatsForDate(startDate, tickerListName));
                    context.put("tickerListName", tickerListName);
                    context.put("priceDate", startDate);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public void loadSourceTableDataForTicker(Map<String,Object> context, RuleDTO rule, String ticker, LocalDate startDate, String tickerListName){
        List<String> sourceTables = List.of(rule.getSourceTables().split(","));
        for (String sourceTable : sourceTables) {
            switch(sourceTable.trim() ){  
                case "STOCK_PRICE" -> {
                    List<StockPrice> existingStockPrices = (List<StockPrice>) context.computeIfAbsent("stockPriceList", k -> new ArrayList<>());
                    existingStockPrices.addAll(getStockPricesForTicker(ticker, startDate));
                }
                case "STOCK_STATS" -> {
                    List<StockStats> existingStockStats = (List<StockStats>) context.computeIfAbsent("stockStatsList", k -> new ArrayList<>());
                    existingStockStats.addAll(getStockStatsForTicker(ticker, startDate));
                }
                case "df" -> {
                    context.put("df", DF_DDMMMYYYY);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> loadSourceDestDataForTickers(RuleDTO rule, List<String> tickers, LocalDate startDate){

        List<Map<String, Object>> allContexts = new ArrayList<>(tickers.size());
        List<String> sourceTables = List.of(rule.getSourceTables().split(","));

        Map<String, List<StockPrice>> pricesByTicker = sourceTables.contains("STOCK_PRICE")
                ? stockPriceService.getStockPricesForTickers(tickers, startDate)
                : Collections.emptyMap();

        Map<String, List<StockStats>> statsByTicker = sourceTables.contains("STOCK_STATS")
                ? stockStatsService.getStockStatsForTickers(tickers, startDate)
                : Collections.emptyMap();

        tickers.forEach(ticker -> {
            Map<String, Object> context = new HashMap<>();
            for (String sourceTable : sourceTables) {
                switch(sourceTable.trim() ){
                    case "STOCK_PRICE" -> {
                        List<StockPrice> existingStockPrices = (List<StockPrice>) context.computeIfAbsent("stockPriceList", k -> new ArrayList<>());
                        existingStockPrices.addAll(pricesByTicker.get(ticker) == null? new ArrayList<StockPrice>(): pricesByTicker.get(ticker) );
                    }
                    case "STOCK_STATS" -> {
                        List<StockStats> existingStockStats = (List<StockStats>) context.computeIfAbsent("stockStatsList", k -> new ArrayList<>());
                        existingStockStats.addAll(statsByTicker.get(ticker) == null? new ArrayList<StockStats>():statsByTicker.get(ticker));
                    }
                    case "df" -> {
                        context.put("df", DF_DDMMMYYYY);
                    }
                }
                loadDestTable(context, rule, null);
                allContexts.add(context);
            }
        });
        return allContexts;
    }


    public void loadDestTable(Map<String,Object> context, RuleDTO rule, String tickerListName){
        List<String> sourceTables = List.of(rule.getSourceTables().split(","));
        String destinationTable = rule.getDestinationTable();
        if("STOCK_PRICE".equals(destinationTable) && sourceTables.contains("LOAD_BSP_TICKER")){
            List<?> bspList = (List<?>) context.get("bspList");
            if (bspList != null) {
                List<StockPrice> target = Stream.generate(StockPrice::new)
                        .limit(bspList.size())
                        .collect(Collectors.toCollection(() -> new ArrayList<>(bspList.size())));
                context.put("stockPriceList", target);
            }
        }
        if("STOCK_STATS".equals(destinationTable) && sourceTables.contains("STOCK_PRICE")){
            List<?> stockPrices = (List<?>) context.get("stockPriceList");
            if (stockPrices != null) {
                List<StockStats> target = Stream.generate(StockStats::new)
                        .limit(stockPrices.size())
                        .collect(Collectors.toCollection(() -> new ArrayList<>(stockPrices.size())));
                context.put("stockStatsList", target);
            }
        }
        if("MOMENTUM_STATS".equals(destinationTable) && sourceTables.contains("STOCK_PRICE")){
            List<?> stockPrices = (List<?>) context.get("stockPriceList");
            if (stockPrices != null) {
                List<MomentumStats> target = Stream.generate(MomentumStats::new)
                        .limit(stockPrices.size())
                        .collect(Collectors.toCollection(() -> new ArrayList<>(stockPrices.size())));
                context.put("momentumStatsList", target);
            }
        }
        if("MOMENTUM_Z_STATS".equals(destinationTable) && sourceTables.contains("MOMENTUM_STATS")){
            context.put("momentumZStats", new MomentumZStats());
        }
       if("MOMENTUM_Z_STATS_SCORE".equals(destinationTable) && sourceTables.contains("MOMENTUM_Z_STATS")){
            List<?> momentumStatsList = (List<?>) context.get("momentumStatsList");
            if (momentumStatsList != null) {
                List<MomentumZStatsScore> target = Stream.generate(MomentumZStatsScore::new)
                        .limit(momentumStatsList.size())
                        .collect(Collectors.toCollection(() -> new ArrayList<>(momentumStatsList.size())));
                context.put("momentumZStatsScoreList", target);
            }else context.put("momentumZStatsScoreList", new ArrayList<>());
        }
    }


    public List<LoadBspTickerDTO> getLoaderBsp(LocalDate date, List<String> tickers){
        String baseUrl = this.loaderUrl + "/bsp/search?date={date}&tickers={ticker}&size={limit}&page={page}";
        List<LoadBspTickerDTO> bspTickers = new ArrayList<>();
        int currentPage = 0;
        BspSearchResponse pageResponse;
        tickers = (tickers != null && tickers.size()== 1 && tickers.get(0).equals("***ALL***"))? null: tickers;
        do {
            log.trace("--- Fetching Page {} ---", currentPage);
            String jsonResult = restTemplate.getForObject(
                baseUrl,
                String.class,
                date,
                tickers,
                LIMIT,
                currentPage
            );
            log.trace("JsonResult = {}", jsonResult);
            pageResponse = objectMapper.readValue(jsonResult, BspSearchResponse.class);
            List<LoadBspTickerDTO> loadBspTickers = pageResponse.getContent();
            log.trace("loadbsptickers {}",loadBspTickers);
            currentPage++;
            bspTickers.addAll(loadBspTickers);
            log.trace("Fetched records: " + (loadBspTickers != null ? loadBspTickers.size() : 0));
        } while (!pageResponse.isLast());
        return bspTickers;
    }



    public List<MomentumStats> getMomentumStatsForDate(LocalDate date, List<String> tickers){
        int pageNumber = 0, pageSize = LIMIT;
        Page<MomentumStats> momentumStatsPage;
        List<MomentumStats> momentumStats = new ArrayList<>();
        do {
            PageRequest pageable = PageRequest.of(pageNumber, pageSize);
            momentumStatsPage = momentumStatsService.getMomentumStatsForDate(date, tickers, pageable);
            momentumStats.addAll(momentumStatsPage.getContent());
            pageNumber++;
        } while (momentumStatsPage.hasNext());
        return momentumStats;
    }


    public MomentumZStats getMomentumZStatsForDate(LocalDate date, String tickerListName){
        MomentumZStats momentumZStats = momentumZStatsService.getStockStatsForDate(date, tickerListName);
        return momentumZStats;
    }


    public List<StockPrice> getStockPricesForTicker(String ticker, LocalDate date){
        int pageNumber = 0, pageSize = LIMIT;
        Page<StockPrice> stockPricePage;
        List<StockPrice> stockPrices = new ArrayList<>();
        do {
            PageRequest pageable = PageRequest.of(pageNumber, pageSize);
            stockPricePage = stockPriceService.getByTicker(ticker, date, pageable);
            stockPrices.addAll(stockPricePage.getContent());
            pageNumber++;
        } while (stockPricePage.hasNext());
        return stockPrices;
    }

    
    public List<StockStats> getStockStatsForTicker(String ticker, LocalDate date){
        int pageNumber = 0, pageSize = LIMIT;
        Page<StockStats> stockStatsPage;
        List<StockStats> stockStats = new ArrayList<>();
        do {
            PageRequest pageable = PageRequest.of(pageNumber, pageSize);
            stockStatsPage = stockStatsService.findByTicker(ticker, date, pageable);
            stockStats.addAll(stockStatsPage.getContent());
            pageNumber++;
        } while (stockStatsPage.hasNext());
        return stockStats;
    }

}
