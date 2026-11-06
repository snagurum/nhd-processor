package com.nhd.processor.services.repository;

import com.nhd.processor.models.TickerList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TickerListRepository extends JpaRepository<TickerList, Long> {
    Optional<TickerList> findByName(String name);
    boolean existsByName(String name);
} 