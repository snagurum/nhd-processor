package com.nhd.processor.models;

import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.sql.Date;

@Data
@Entity
@Table(name = "stocks", schema = "lt")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private String name;

    private String series;

    private Date dateOfListing;

    private boolean historyLoaded;

    private boolean active;

}
