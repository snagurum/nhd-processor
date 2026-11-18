package com.nhd.processor.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.dto.DataQuery;
import com.nhd.processor.dto.DataPage;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class StockPriceService {
    private final StocksDataClient client;
    private final ProcessorProperties properties;

    public void saveAll(List<StockPrice> rows) {
        client.save(
            "/internal/stock-prices/batch"
            , rows
        );
    }

    public List<StockPrice> getStockPricesFromDate(LocalDate date, List<String> tickers) {
        List<StockPrice> results = new ArrayList<>();
        Page<StockPrice> page;
        int pageNumber = 0;
        do {
            page = getStockPricesFromDate(date
                , tickers
                , PageRequest.of(pageNumber++, properties.getPaginationPageSize())
            );
            results.addAll(page.getContent());
        } while (page.hasNext());
        return results;
    }

    public Page<StockPrice> getStockPricesFromDate(LocalDate date, List<String> tickers, Pageable pageable) {
        DataQuery query = new DataQuery(
            date
            , tickers
            , null
            , null
            , pageable.getPageNumber()
            , pageable.getPageSize()
        );
        return client.page(
            "/internal/stock-prices/date"
            , query
            , pageable
            , new ParameterizedTypeReference<DataPage<StockPrice>>() {}
        );
    }

    public Page<StockPrice> getByTicker(String ticker, LocalDate date, Pageable pageable) {
        DataQuery query = new DataQuery(
            date
            , null
            , ticker
            , null
            , pageable.getPageNumber()
            , pageable.getPageSize()
        );
        return client.page(
            "/internal/stock-prices/ticker"
            , query
            , pageable
            , new ParameterizedTypeReference<DataPage<StockPrice>>() {}
        );
    }

    public Map<String, List<StockPrice>> getStockPricesForTickers(List<String> tickers, LocalDate date) {
        DataQuery query = new DataQuery(
            date
            , tickers
            , null
            , null
            , 0
            , properties.getPaginationPageSize()
        );
        return client.post(
            "/internal/stock-prices/tickers"
            , query
            , new ParameterizedTypeReference<Map<String, List<StockPrice>>>() {}
        );
    }
}
