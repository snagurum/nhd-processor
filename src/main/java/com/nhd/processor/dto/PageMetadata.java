package com.nhd.processor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PageMetadata {
    private Integer size;
    private Integer number;
    private Integer totalElements;
    private Integer totalPages;
}
