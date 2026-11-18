package com.nhd.processor.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record DataPage<T>(List<T> content, long totalElements) {

    public static <T> DataPage<T> from(Page<T> page) {
        return new DataPage<>(page.getContent(), page.getTotalElements());
    }
}
