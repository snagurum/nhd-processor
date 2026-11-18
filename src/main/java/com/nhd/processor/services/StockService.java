package com.nhd.processor.services;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StocksDataClient client;

    public List<String> getAllActiveTickers() {
        return client.post(
            "/internal/active-tickers"
            ,Map.of()
            ,new ParameterizedTypeReference<List<String>>() {}
        );
    }
}
