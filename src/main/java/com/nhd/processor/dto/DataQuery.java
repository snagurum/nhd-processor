package com.nhd.processor.dto;

import java.time.LocalDate;
import java.util.List;


public record DataQuery(LocalDate date
    , List<String> tickers
    , String ticker
    , String tickerListName
    , Integer page
    , Integer size) {
    public DataQuery {
        page = page == null ? 0 : page;
        size = size == null ? 2000 : size;
    }
}
