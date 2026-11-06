package com.nhd.processor.models;

import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "rule", schema = "re")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique=true, nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Column(name = "algo_type", nullable = false, length = 255)
    private String algoType;

    @Column(name = "source_tables", nullable = false, length = 255)
    private String sourceTables;

    @Column(name = "destination_table", nullable = false, length = 255)
    private String destinationTable;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

   @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleItem> ruleItems = new ArrayList<>();

    public void addRuleItem(RuleItem item) {
        ruleItems.add(item);
        item.setRule(this);
    }
}
