package com.nhd.processor.models;


import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "rule_item")
@NoArgsConstructor
public class RuleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String condition;

    // Escaping the reserved PostgreSQL keyword "then"
    @Column(name = "then_value", nullable = false, columnDefinition = "TEXT")
    private String thenValue;

    // Escaping the reserved PostgreSQL keyword "else"
    @Column(name = "else_value", columnDefinition = "TEXT")
    private String elseValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;
}
