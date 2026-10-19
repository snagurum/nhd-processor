package com.nhd.processor.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StockPriceSearchResponse {
    private List<StockPriceDTO> content;
    private PageMetadata page;

    public boolean isLast() {
        if (page == null || page.getTotalPages() == null || page.getNumber() == null) {
            return true;
        }
        return page.getNumber() >= (page.getTotalPages() - 1);
    }
}