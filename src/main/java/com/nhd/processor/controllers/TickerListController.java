package com.nhd.processor.controllers;

import com.nhd.processor.models.TickerList;
import com.nhd.processor.services.repository.TickerListRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ticker-list")
public class TickerListController {

    private final TickerListRepository tickerListRepository;

    public TickerListController(
            TickerListRepository tickerListRepository) {
        this.tickerListRepository = tickerListRepository;
    }

    @GetMapping
    public ResponseEntity<List<TickerList>> getAllTickerLists() {
        return ResponseEntity.ok(
            tickerListRepository.findAll()
        );
    }
}
