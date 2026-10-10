package com.nhd.processor.dto;

import java.util.List;

import lombok.Data;

@Data
public class RuleDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private List<RuleItemDTO> ruleItems;
}