package com.nhd.processor.services;

import com.nhd.processor.models.StockPrice;
import com.nhd.processor.repository.StockPriceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@Service
public class StockPriceService {
    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    private StockPriceRepository stockPriceRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<StockPrice> findByPriceDate(LocalDate priceDate, Pageable pageable){
        return stockPriceRepository.findByPriceDate(priceDate, pageable);
    }

    public Page<StockPrice> getStockPricesFromDate(LocalDate date, List<String> tickers, Pageable pageable){
        Page<StockPrice> stockPricePage;
        if(null == tickers || tickers.size() == 0) stockPricePage = stockPriceRepository.findByPriceDate(date, pageable);
        else if (tickers.size() > 1)
            stockPricePage = stockPriceRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
        else
            stockPricePage = stockPriceRepository.findByPriceDateAndTickerStartingWith(date, ""+tickers.get(0).charAt(0), pageable);
        return stockPricePage;
    }

    public Page<StockPrice> getByTicker(String ticker, LocalDate fromDate, Pageable pageable){
        if(fromDate == null)
            return stockPriceRepository.findByTickerOrderByPriceDateAsc(ticker, pageable);
        else return stockPriceRepository.findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(ticker, fromDate, pageable);
    }

    public Page<StockPrice> findByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable){
        return stockPriceRepository.findByPriceDateAndTickerStartingWith(date, ticker, pageable);
    }

    public Page<StockPrice> findByPriceDateAndTickerIn(LocalDate date, List<String> tickers, Pageable pageable){
        return stockPriceRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
    }

    public Page<StockPrice> findByTickerAndPriceDateGreaterThanEqual(String ticker, LocalDate date, Pageable pageable){
        return stockPriceRepository.findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(ticker, date, pageable);
    }
/**
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
*/

    @Transactional
    public void saveAll(List<StockPrice> stockPrice) {
        if (stockPrice == null || stockPrice.isEmpty()) {
            return;
        }

        entityManager.createNativeQuery("""
            CREATE TEMP TABLE temp_stock_price (
                ticker           VARCHAR(50),
                price_date       DATE,
                series           VARCHAR(10),
                open             NUMERIC(10, 2),
                high             NUMERIC(10, 2),
                low              NUMERIC(10, 2),
                prev_close       NUMERIC(10, 2),
                close            NUMERIC(10, 2),
                vwap             NUMERIC(10, 2),
                high52           NUMERIC(10, 2),
                low52            NUMERIC(10, 2),
                volume           BIGINT,
                value            NUMERIC(15, 2),
                no_of_trades     BIGINT,
                ltp              NUMERIC(10, 2),
                ffmc             BIGINT
            ) ON COMMIT DROP;
        """).executeUpdate();

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO temp_stock_price (ticker, price_date, series, open, high, low, prev_close, close, vwap, high52, low52, volume, value, no_of_trades, ltp, ffmc) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchSize = 10000;
                int count = 0;

                for (StockPrice ss : stockPrice) {
                    ps.setString    (1, ss.getTicker());
                    ps.setDate      (2, java.sql.Date.valueOf(ss.getPriceDate()));
                    ps.setString    (3, ss.getSeries());
                    ps.setBigDecimal(4, ss.getOpen());
                    ps.setBigDecimal(5, ss.getHigh());
                    ps.setBigDecimal(6, ss.getLow());
                    ps.setBigDecimal(7, ss.getPrevClose());
                    ps.setBigDecimal(8, ss.getClose());
                    ps.setBigDecimal(9, ss.getVwap());
                    ps.setBigDecimal(10, ss.getHigh52());
                    ps.setBigDecimal(11, ss.getLow52());
                    ps.setObject    (12, ss.getVolume());
                    ps.setBigDecimal(13, ss.getValue());
                    ps.setObject    (14, ss.getNoOfTrades());
                    ps.setBigDecimal(15, ss.getLtp());
                    ps.setObject    (16, ss.getFfmc());
                    ps.addBatch();
                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            }
        });
        entityManager.flush();

        entityManager.createNativeQuery("""
            INSERT INTO nse.stock_price (ticker, price_date, series, open, high, low, prev_close, close, vwap, high52, low52, volume, value, no_of_trades, ltp, ffmc)
            SELECT ticker, price_date, series, open, high, low, prev_close, close, vwap, high52, low52, volume, value, no_of_trades, ltp, ffmc
            FROM temp_stock_price
            ON CONFLICT (ticker, price_date)
            DO UPDATE SET
              series        = COALESCE(EXCLUDED.series       , stock_price.series         ),
              open          = COALESCE(EXCLUDED.open         , stock_price.open           ),
              high          = COALESCE(EXCLUDED.high         , stock_price.high           ),
              low           = COALESCE(EXCLUDED.low          , stock_price.low            ),
              prev_close    = COALESCE(EXCLUDED.prev_close   , stock_price.prev_close     ),
              close         = COALESCE(EXCLUDED.close        , stock_price.close          ),
              vwap          = COALESCE(EXCLUDED.vwap         , stock_price.vwap           ),
              high52        = COALESCE(EXCLUDED.high52       , stock_price.high52         ),
              low52         = COALESCE(EXCLUDED.low52        , stock_price.low52          ),
              volume        = COALESCE(EXCLUDED.volume       , stock_price.volume         ),
              value         = COALESCE(EXCLUDED.value        , stock_price.value          ),
              no_of_trades  = COALESCE(EXCLUDED.no_of_trades , stock_price.no_of_trades   ),
              ltp           = COALESCE(EXCLUDED.ltp          , stock_price.ltp            ),
              ffmc          = COALESCE(EXCLUDED.ffmc         , stock_price.ffmc           );
            """).executeUpdate();

        entityManager.clear();
    }
}
