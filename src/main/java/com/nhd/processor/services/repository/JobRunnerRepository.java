package com.nhd.processor.services.repository;

import com.nhd.processor.models.JobRunner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface JobRunnerRepository extends JpaRepository<JobRunner, Long> {
    
    Optional<JobRunner> findByName(String name);
    
    Optional<JobRunner> findById(Long id);

    List<JobRunner> findAll();

    boolean existsByName(String name);

    boolean existsByTickerListName(String tickerListName);

}