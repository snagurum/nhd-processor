package com.nhd.processor.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.jobs.services.DbPersistenceService;
import com.nhd.processor.models.StrategyPortfolio;
import com.nhd.processor.models.StrategyTrade;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortfolioServiceTest {

    private static final String BASE_URL = "http://localhost:8083/pf";
    private final RestTemplate http = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(http).build();
    private final PortfolioService service = new PortfolioService(http, BASE_URL + "/");

    @Test
    void flushesBothStrategyDestinationsAndRetriesOnlyPendingRows() {
        var persistence = new DbPersistenceService(null, null, null, service, new ProcessorProperties());
        var buffer = persistence.newBuffer();
        Map<String, Object> context = new HashMap<>();
        context.put("strategyPortfolioList", List.of(StrategyPortfolio.builder().strategyRunnerId(7L).build()));
        context.put("strategyTradeList", List.of(StrategyTrade.builder().strategyRunnerId(7L).build()));

        persistence.cacheData(buffer, context, "STRATEGY_PORTFOLIO, STRATEGY_TRADE");
        assertTrue(context.isEmpty());
        persistence.flushData(buffer, false);

        server.expect(requestTo(BASE_URL + "/strategy-portfolios"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"id\":12}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/strategy-trades"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());
        assertThrows(HttpServerErrorException.class, () -> persistence.flushData(buffer, true));
        server.verify();
        server.reset();

        server.expect(requestTo(BASE_URL + "/strategy-trades"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"id\":21}", MediaType.APPLICATION_JSON));
        persistence.flushData(buffer, true);
        persistence.flushData(buffer, true);
        server.verify();
    }

    @Test
    void createsPortfolioUsingRunnerIdAndReturnsGeneratedId() {
        server.expect(requestTo(BASE_URL + "/strategy-portfolios"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.strategyRunnerId").value(7))
            .andExpect(jsonPath("$.executionDate").value("2026-11-18"))
            .andRespond(withSuccess("""
                {"id":12,"strategyRunnerId":7,"executionDate":"2026-11-18"}
                """, MediaType.APPLICATION_JSON));

        StrategyPortfolio saved = service.savePortfolio(StrategyPortfolio.builder()
            .strategyRunnerId(7L)
            .executionDate(LocalDate.of(2026, 11, 18))
            .build());

        assertEquals(12L, saved.getId());
        assertEquals(LocalDate.of(2026, 11, 18), saved.getExecutionDate());
        server.verify();
    }

    @Test
    void updatesTradeUsingTradeIdAndRunnerId() {
        server.expect(requestTo(BASE_URL + "/strategy-trades/21"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(jsonPath("$.id").value(21))
            .andExpect(jsonPath("$.strategyRunnerId").value(7))
            .andExpect(jsonPath("$.stockName").value("ABC"))
            .andRespond(withSuccess("""
                {"id":21,"strategyRunnerId":7,"stockName":"ABC"}
                """, MediaType.APPLICATION_JSON));

        StrategyTrade saved = service.saveTrade(StrategyTrade.builder()
            .id(21L)
            .strategyRunnerId(7L)
            .stockName("ABC")
            .build());

        assertEquals("ABC", saved.getStockName());
        server.verify();
    }
}
