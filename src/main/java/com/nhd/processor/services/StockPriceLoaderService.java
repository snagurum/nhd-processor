package com.nhd.processor.services;



import com.nhd.processor.dto.BspSearchResponse;
import com.nhd.processor.dto.LoadBspTickerDTO;
import com.nhd.processor.dto.StockPriceDTO;
import com.nhd.processor.dto.StockPriceSearchResponse;
import com.nhd.processor.models.StockPrice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StockPriceLoaderService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceLoaderService.class);
    private static final int limit = 2000;
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
        List<LoadBspTickerDTO> accumulatedBsps = new ArrayList<>();
        Map<LocalDate, LocalDate> tradingDates = getDistinctTradingDates();

        if( null == fromDate ){
            startDate = LocalDate.parse("01-Apr-1997", DATE_FORMATTER);
        }else{
            startDate = fromDate;
        }


        startDate.datesUntil(endDate).forEach(currentDate -> {
            if(tradingDates.containsKey(currentDate)){
                processStockPriceForDate0(currentDate, ticker, accumulatedBsps);
            }
        });

        if (!accumulatedBsps.isEmpty()) {
            flushAccumulatedData(accumulatedBsps);
        }
    }

    private Map<LocalDate, LocalDate> getDistinctTradingDates(){
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

    public void processStockPriceForDate0(LocalDate date, String character, List<LoadBspTickerDTO> accumulatedBsps){
        List<LoadBspTickerDTO> bspDtos = this.getLoaderBsp(date, character);
        if(bspDtos.isEmpty()){
            log.info("No Data available for ticker startwith = {} on date = {}", character, date);
            return;
        }
        accumulatedBsps.addAll(bspDtos);
        if (accumulatedBsps.size() > 4000) {
            log.info("SP Loader Processed total accumulated bsp ({}), date({}})", accumulatedBsps.size(), date);
            flushAccumulatedData(accumulatedBsps);
        }
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

    private void flushAccumulatedData(List<LoadBspTickerDTO> accumulatedBsps) {
        List<StockPrice> stockPriceList = executeRuleEngine(accumulatedBsps);
        stockPriceService.saveAll(stockPriceList);
        accumulatedBsps.clear();
    }

    public List<StockPrice> executeRuleEngine(List<LoadBspTickerDTO> bspList){
        List<StockPrice> stockPricesList = new ArrayList<>(bspList.size());
        for (int i = 0; i < bspList.size(); i++) {
            stockPricesList.add(new StockPrice());
        }

        Map<String, Object> context = new HashMap<>();
        context.put("bspList", bspList);
        context.put("stockPriceList", stockPricesList);
        context.put("df", DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));

        ruleEngineService.executeRule(context, ruleService.getRulesByName("SP Loader"));
        return stockPricesList;

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

}

