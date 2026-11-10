package com.nhd.processor.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhd.processor.models.Rule;


public interface RuleRepository extends JpaRepository<Rule, Long> {
    
    Rule findByName(String name);

    List<Rule> findByNameContainingIgnoreCase(String name);
}
