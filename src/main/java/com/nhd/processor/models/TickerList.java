package com.nhd.processor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "Ticker_List", schema = "re")
public class TickerList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "ticker_list", columnDefinition = "TEXT")
    private String tickerList;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "active_from", nullable = false)
    private LocalDate activeFrom;
}
