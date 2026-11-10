package com.nhd.processor.services;

import com.nhd.processor.repository.StockRepository;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.util.List;

@Service
@AllArgsConstructor
public class StockService {
    
    private final StockRepository stockRepository;

    public List<String> getAllActiveTickers(){
        return stockRepository.findTickersByActive();
    }
}
