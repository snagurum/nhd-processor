package com.nhd.processor.services.repository;

import com.nhd.processor.models.Stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("SELECT s.ticker FROM Stock s WHERE s.active = :active")
    List<String> findTickersByActive(@Param("active") boolean active);

    default List<String> findTickersByActive() {
        return findTickersByActive(true);
    }

}
