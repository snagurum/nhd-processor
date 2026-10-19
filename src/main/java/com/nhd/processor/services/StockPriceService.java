package com.nhd.processor.services;

import com.nhd.processor.models.StockPrice;
import com.nhd.processor.services.repository.StockPriceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StockPriceService {
    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    private StockPriceRepository stockPriceRepository;

    public void saveAll(List<StockPrice> stockPrices){
        if (stockPrices == null || stockPrices.isEmpty()) return;
    
        List<StockPrice> toBeStockPrices = new ArrayList<>();
        LocalDate priceDate = stockPrices.get(0).getPriceDate();
        String tickerStartsWith = ""+stockPrices.get(0).getTicker().charAt(0);
        final AtomicInteger newRecordCount = new AtomicInteger(0);
        final AtomicInteger existingRecordCount = new AtomicInteger(0);

        List<StockPrice> existingStockPrices = stockPriceRepository.findByPriceDateAndTickerStartingWith(
            priceDate,
            tickerStartsWith,
            null
        ).getContent();
        Map<String, StockPrice> existingMap = existingStockPrices.stream()
            .collect(Collectors.toMap(
                sp -> sp.getTicker() + "_" + sp.getPriceDate(),
                sp -> sp,
                (existing, replacement) -> existing
        ));
        stockPrices.forEach(sp -> {
            String key = sp.getTicker() + "_" + sp.getPriceDate();
            if (existingMap.containsKey(key)) {
                existingRecordCount.incrementAndGet();
                StockPrice existingRecord = existingMap.get(key);
                existingRecord.setSeries(sp.getSeries());
                existingRecord.setOpen(sp.getOpen());
                existingRecord.setHigh(sp.getHigh());
                existingRecord.setLow(sp.getLow());
                existingRecord.setPrevClose(sp.getPrevClose());
                existingRecord.setClose(sp.getClose());
                existingRecord.setVwap(sp.getVwap());
                existingRecord.setHigh52(sp.getHigh52());
                existingRecord.setLow52(sp.getLow52());
                existingRecord.setVolume(sp.getVolume());
                existingRecord.setValue(sp.getValue());
                existingRecord.setNoOfTrades(sp.getNoOfTrades());
                existingRecord.setLtp(sp.getLtp());
                if(null != sp.getFfmc()) existingRecord.setFfmc(sp.getFfmc());
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
        stockPriceRepository.saveAll(toBeStockPrices);
    }


    public Map<String, StockPrice> getExistingStockPrices(LocalDate priceDate, String tickerPrefix, Pageable pageable){

        List<StockPrice> stockPrices = stockPriceRepository.findByPriceDateAndTickerStartingWith(
            priceDate,
            tickerPrefix,
            null
        ).getContent();

        Map<String, StockPrice> existingMap = stockPrices.stream()
            .collect(Collectors.toMap(
                sp -> sp.getTicker() + "_" + sp.getPriceDate(),
                sp -> sp,
                (existing, replacement) -> existing
        ));
        return existingMap;
    }
}
