package com.nhd.processor.services;

import com.nhd.processor.dto.DataPage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

/** HTTP boundary for stock data. Failed requests propagate to the job runner. */
@Component
public class StocksDataClient {
    private final RestTemplate http;
    private final String url;
    private final int batchSize;

    public StocksDataClient(RestTemplate http, @Value("${app.stocksdata.url}") String url,
            @Value("${processor.stocksdata-write-batch-size:2000}") int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("stocksdata write batch size must be positive");
        }
        this.http = http;
        this.url = url.replaceAll("/+$", "");
        this.batchSize = batchSize;
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<T> type) {
        return Objects.requireNonNull(
                http.exchange(url + path, HttpMethod.POST, new HttpEntity<>(body), type).getBody(),
                "Empty stocksdata response");
    }

    public <T> Page<T> page(String path, Object body, Pageable pageable,
            ParameterizedTypeReference<DataPage<T>> type) {
        DataPage<T> result = post(path, body, type);
        return new PageImpl<>(result.content(), pageable, result.totalElements());
    }

    public void save(String path, List<?> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        // Each chunk commits independently. Existing upserts make replay safe after partial failure.
        for (int i = 0; i < rows.size(); i += batchSize) {
            List<?> chunk = rows.subList(i, Math.min(i + batchSize, rows.size()));
            http.exchange(url + path, HttpMethod.POST, new HttpEntity<>(chunk), Void.class);
        }
    }
}
