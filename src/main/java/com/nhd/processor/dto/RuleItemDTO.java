package com.nhd.processor.dto;



import lombok.Data;

@Data
public class RuleItemDTO {
    private Long id;
    private String condition;
    private String thenValue;
    private String elseValue;
}

