package com.nhd.processor.services;

import com.nhd.processor.models.StrategyPortfolio;
import com.nhd.processor.models.StrategyTrade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class PortfolioService {

    private final RestTemplate http;
    private final String baseUrl;

    public PortfolioService(RestTemplate http, @Value("${app.portfolio.url}") String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public StrategyPortfolio savePortfolio(StrategyPortfolio portfolio) {
        return save("/strategy-portfolios", portfolio.getId(), portfolio, StrategyPortfolio.class);
    }

    public StrategyTrade saveTrade(StrategyTrade trade) {
        return save("/strategy-trades", trade.getId(), trade, StrategyTrade.class);
    }

    private <T> T save(String path, Long id, T body, Class<T> responseType) {
        String url = baseUrl + path;
        T saved = id == null
            ? http.postForObject(url, body, responseType)
            : http.exchange(url + "/{id}", HttpMethod.PUT, new HttpEntity<>(body), responseType, id).getBody();
        return Objects.requireNonNull(saved, "Empty nhd-portfolio response");
    }
}
