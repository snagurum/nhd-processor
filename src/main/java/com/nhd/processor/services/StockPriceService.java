package com.nhd.processor.services;

import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;
import com.nhd.processor.services.repository.StockPriceRepository;

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
public class StockPriceService {
    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    private StockPriceRepository stockPriceRepository;

    public void saveAll(List<StockPrice> stockPrices){
        if (stockPrices == null || stockPrices.isEmpty()) return;
    
        List<StockPrice> toBeStockPrices = new ArrayList<>();
        List<LocalDate> distinctPriceDates = stockPrices.stream()
            .map(StockPrice::getPriceDate)
            .distinct()
            .collect(Collectors.toList());

        Map<String, StockPrice> existingMap = getExistingStockPrices(distinctPriceDates);
        stockPrices.forEach(sp -> {
            String key = sp.getTicker() + "_" + sp.getPriceDate();
            if (existingMap.containsKey(key)) {
                toBeStockPrices.add(updateExistingStockPrice(existingMap.get(key), sp));
            } else {
                toBeStockPrices.add(sp);
            }
        });
        stockPriceRepository.saveAll(toBeStockPrices);
    }


    public Map<String, StockPrice> getExistingStockPrices(List<LocalDate> priceDates){

        Map<String, StockPrice> existingMap = new HashMap<>();
        for (LocalDate priceDate : priceDates) {
            List<StockPrice> stockPrices = stockPriceRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();

            Map<String, StockPrice> tempMap = stockPrices.stream()
                .collect(Collectors.toMap(
                    sp -> sp.getTicker() + "_" + sp.getPriceDate(),
                    sp -> sp,
                    (existing, replacement) -> existing
            ));
            existingMap.putAll(tempMap);
        }
        return existingMap;
    }

    public Page<StockPrice> findByPriceDate(LocalDate priceDate, Pageable pageable){
        return stockPriceRepository.findByPriceDate(priceDate, pageable);
    }

    private StockPrice updateExistingStockPrice(StockPrice oldSp, StockPrice newSp){
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
        if(null != newSp.getFfmc())         oldSp.setFfmc(newSp.getFfmc());
        return oldSp;
    }
}
