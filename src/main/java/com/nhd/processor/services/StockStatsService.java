package com.nhd.processor.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.models.StockStats;
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

/** Supplies rule contexts and persists rule output through stocksdata HTTP APIs. */
@Service
@RequiredArgsConstructor
public class StockStatsService {
    private final StocksDataClient client;
    private final ProcessorProperties properties;

    public void saveAll(List<StockStats> rows) {
        client.save(
            "/internal/stock-stats/batch"
            , rows
        );
    }

    public List<StockStats> getStockStatsFromDate(LocalDate date, List<String> tickers) {
        List<StockStats> results = new ArrayList<>();
        Page<StockStats> page;
        int pageNumber = 0;
        do {
            page = getStockStatsFromDate(date
                , tickers
                , PageRequest.of(pageNumber++, properties.getPaginationPageSize())
            );
            results.addAll(page.getContent());
        } while (page.hasNext());
        return results;
    }

    public Page<StockStats> getStockStatsFromDate(LocalDate date, List<String> tickers, Pageable pageable) {
        DataQuery query = new DataQuery(
            date
            , tickers
            , null
            , null
            , pageable.getPageNumber()
            , pageable.getPageSize()
        );
        return client.page(
            "/internal/stock-stats/date"
            , query
            , pageable
            , new ParameterizedTypeReference<DataPage<StockStats>>() {}
        );
    }

    public Page<StockStats> findByTicker(String ticker, LocalDate date, Pageable pageable) {
        DataQuery query = new DataQuery(
            date
            , null
            , ticker
            , null
            , pageable.getPageNumber()
            , pageable.getPageSize()
        );
        return client.page(
            "/internal/stock-stats/ticker"
            , query
            , pageable
            , new ParameterizedTypeReference<DataPage<StockStats>>() {}
        );
    }

    public Map<String, List<StockStats>> getStockStatsForTickers(List<String> tickers, LocalDate date) {
        DataQuery query = new DataQuery(
            date
            , tickers, null
            , null
            , 0
            , properties.getPaginationPageSize()
        );
        return client.post(
            "/internal/stock-stats/tickers"
            , query
            , new ParameterizedTypeReference<Map<String, List<StockStats>>>() {}
        );
    }
}
