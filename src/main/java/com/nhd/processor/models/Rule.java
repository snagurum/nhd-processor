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

// import org.springframework.data.annotation.Id;
// import org.springframework.data.relational.core.mapping.Column;
// import org.springframework.data.relational.core.mapping.Table;

@Data
@Entity
@Table(name = "rule", schema = "re")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // CascadeType.ALL handles automatic saving/deleting of child items
   @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleItem> ruleItems = new ArrayList<>();

    // Helper methods to keep both sides of the relationship synchronized
    public void addRuleItem(RuleItem item) {
        ruleItems.add(item);
        item.setRule(this);
    }
}
