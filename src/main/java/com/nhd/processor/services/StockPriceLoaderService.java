package com.nhd.processor.services;



import com.nhd.processor.dto.BspSearchResponse;
import com.nhd.processor.dto.LoadBspTickerDTO;
import com.nhd.processor.dto.StockPriceDTO;
import com.nhd.processor.dto.StockPriceSearchResponse;
import com.nhd.processor.models.StockPrice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
public class StockPriceLoaderService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceLoaderService.class);
    private static final int limit = 100;
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yyyy")
            .toFormatter(Locale.ENGLISH);
    private static final List<String> ALPHANUMERIC_CHARACTERS = IntStream.concat(
                IntStream.rangeClosed('0', '9'),
                IntStream.rangeClosed('A', 'Z')
        )
        .mapToObj(c -> String.valueOf((char) c))
        .toList();

    private final String loaderUrl;
    private final String nseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RuleEngineService ruleEngineService;
    private final RuleService ruleService;
    private final StockPriceService stockPriceService;

    public StockPriceLoaderService(
            @Value("${app.loader.url}") String loaderUrl,
            @Value("${app.nse.url}") String nseUrl,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            RuleEngineService ruleEngineService,
            RuleService ruleService,
            StockPriceService stockPriceService) {
        this.loaderUrl = loaderUrl;
        this.nseUrl = nseUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ruleEngineService =ruleEngineService;
        this.ruleService = ruleService;
        this.stockPriceService = stockPriceService;
    }

    public void processStockPriceForDate(LocalDate fromDate, String ticker) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = null;
        if( null == fromDate ){
            startDate = LocalDate.parse("01-Apr-1997", DATE_FORMATTER);
        }else{
            startDate = fromDate;
        }

        if(null == ticker){
            startDate.datesUntil(endDate).forEach(currentDate -> {
                ALPHANUMERIC_CHARACTERS.forEach(item -> processStockPriceForDate0(currentDate, item));
            });
        }else{
            startDate.datesUntil(endDate).forEach(currentDate -> processStockPriceForDate0(currentDate, ticker));
        }
    }

    public void processStockPriceForDate0(LocalDate date, String character){
        List<LoadBspTickerDTO> bspDtos = this.getLoaderBsp(date, character);
        if(bspDtos.isEmpty()){
            log.info("No Data available for ticker startwith = {} on date = {}", character, date);
            return;
        }
        List<StockPrice> stockPrices = new ArrayList<>();
        log.info("Character = {}, Date = {}, from LOAD_BSP_TICKER Count = {}", character, date, bspDtos.size());
        bspDtos.forEach(item ->{
            StockPrice sp = new StockPrice();
            Map<String,Object> context = new HashMap<>();
            context.put("bsp",item);
            context.put("stockprice",sp);
            context.put("df", DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));
            ruleEngineService.executeRule(context, ruleService.getRulesByName("BSP Loader"));
            stockPrices.add((StockPrice)context.get("stockprice"));
        });

        stockPriceService.saveAll(stockPrices);

        // String baseUrl = this.nseUrl + "/api/v1/sp";
        // String response = restTemplate.postForObject(baseUrl, spDTOs, String.class);
        // log.info("processed Character = {}, Date = {}, Count = {}", character, date, bspDtos.size());
    }

    public List<LoadBspTickerDTO> getLoaderBsp(LocalDate date, String tickerStartCharacter){
        String baseUrl = this.loaderUrl + "/bsp/search?date={date}&ticker={ticker}&size={limit}&page={page}";
        List<LoadBspTickerDTO> bspTickers = new ArrayList<>();
        int currentPage = 0;
        BspSearchResponse pageResponse;
        do {
            log.trace("--- Fetching Page {} ---", currentPage);
            String jsonResult = restTemplate.getForObject(
                baseUrl,
                String.class,
                date,
                tickerStartCharacter,
                limit,
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

    public List<StockPriceDTO> getNseStockPrice(LocalDate date){
        String baseUrl = this.nseUrl + "/api/v1/sp/search?date={date}&size={limit}&page={page}";
        List<StockPriceDTO> stockPrices = new ArrayList<>();
        int currentPage = 0;
        StockPriceSearchResponse pageResponse;
        do {
            log.trace("--- Fetching Page {currentPage} ---", currentPage);
            String jsonResult = restTemplate.getForObject(
                baseUrl,
                String.class,
                date,
                limit,
                currentPage
            );
            log.trace("JsonResult = {jsonResult}", jsonResult);
            pageResponse = objectMapper.readValue(jsonResult, StockPriceSearchResponse.class);
            List<StockPriceDTO> tempStockPrices = pageResponse.getContent();
            currentPage++;
            stockPrices.addAll(tempStockPrices);
            log.trace("Fetched records: " + (tempStockPrices != null ? tempStockPrices.size() : 0));
        } while (!pageResponse.isLast());
        return stockPrices;
    }

    public void saveAll(List<StockPrice> stockPrices){
        if (stockPrices == null || stockPrices.isEmpty()) return;

        List<StockPrice> toBeStockPrices = new ArrayList<>();
        LocalDate priceDate = stockPrices.get(0).getPriceDate();
        String tickerStartsWith = ""+stockPrices.get(0).getTicker().charAt(0);
        final AtomicInteger newRecordCount = new AtomicInteger(0);
        final AtomicInteger existingRecordCount = new AtomicInteger(0);

        Map<String, StockPrice> existingMap = stockPriceService.getExistingStockPrices(
            priceDate,
            tickerStartsWith,
            null
        );

        stockPrices.forEach(sp -> {
            String key = sp.getTicker() + "_" + sp.getPriceDate();
            if (existingMap.containsKey(key)) {
                existingRecordCount.incrementAndGet();
                StockPrice existingRecord = existingMap.get(key);
                updateExistingStockPrice(existingRecord, sp);
                toBeStockPrices.add(existingRecord);
            } else {
                newRecordCount.incrementAndGet();
                toBeStockPrices.add(sp);
            }
        });
        log.info("Saving StockPrice for priceDate({}), tickerStart({}), newRecords({}), existingRecords({})"
            , priceDate
            , tickerStartsWith
            , newRecordCount
            , existingRecordCount
        );

        this.stockPriceService.saveAll(toBeStockPrices);
    }

    private void updateExistingStockPrice(StockPrice oldSp, StockPrice newSp){
        if( null != newSp.getSeries() )     oldSp.setSeries(newSp.getSeries());
        if( null != newSp.getOpen() )       oldSp.setOpen(newSp.getOpen());
        if( null != newSp.getHigh() )       oldSp.setHigh(newSp.getHigh());
        if( null != newSp.getLow() )        oldSp.setLow(newSp.getLow());
        if( null != newSp.getPrevClose() )  oldSp.setPrevClose(newSp.getPrevClose());
        if( null != newSp.getClose() )      oldSp.setClose(newSp.getClose());
        if( null != newSp.getVwap() )       oldSp.setVwap(newSp.getVwap());
        if( null != newSp.getHigh52() )     oldSp.setHigh52(newSp.getHigh52());
        if( null != newSp.getLow52() )      oldSp.setLow52(newSp.getLow52());
        if( null != newSp.getVolume() )     oldSp.setVolume(newSp.getVolume());
        if( null != newSp.getValue() )      oldSp.setValue(newSp.getValue());
        if( null != newSp.getNoOfTrades() ) oldSp.setNoOfTrades(newSp.getNoOfTrades());
        if( null != newSp.getLtp() )        oldSp.setLtp(newSp.getLtp());
        if( null != newSp.getFfmc() )       oldSp.setFfmc(newSp.getFfmc());
    }
}

