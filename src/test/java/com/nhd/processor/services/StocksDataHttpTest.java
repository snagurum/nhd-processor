package com.nhd.processor.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import com.nhd.processor.jobs.services.DbPersistenceService;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.re.services.RuleEngineService;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StocksDataHttpTest {
    private MockRestServiceServer server;
    private StocksDataClient client;
    private StockPriceService prices;

    @BeforeEach
    void setup() {
        RestTemplate http = new RestTemplate();
        server = MockRestServiceServer.bindTo(http).build();
        client = new StocksDataClient(http, "http://localhost:8082/stocks/", 2);
        prices = new StockPriceService(client, new ProcessorProperties());
    }

    @Test
    void readsTypedDatePageIncludingDecimalAndDate() {
        server.expect(requestTo("http://localhost:8082/stocks/internal/stock-prices/date"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                    {"date":"2026-01-01","tickers":["TCS","INFY"],"page":1,"size":1}
                    """, false))
                .andRespond(withSuccess("""
                    {"content":[{"ticker":"TCS","priceDate":"2026-01-01","close":123.4500}],"totalElements":3}
                    """, MediaType.APPLICATION_JSON));
        var page = prices.getStockPricesFromDate(LocalDate.of(2026,1,1), List.of("TCS","INFY"), PageRequest.of(1,1));
        assertTrue(page.hasNext());
        assertEquals(3, page.getTotalElements());
        assertEquals(LocalDate.of(2026,1,1), page.getContent().get(0).getPriceDate());
        assertEquals(0, new BigDecimal("123.4500").compareTo(page.getContent().get(0).getClose()));
        server.verify();
    }

    @Test
    void groupedHistoryAndMissingZStatsUseStableWireTypes() {
        server.expect(requestTo("http://localhost:8082/stocks/internal/stock-prices/tickers"))
                .andRespond(withSuccess("""
{"TCS":[{"ticker":"TCS","priceDate":"2026-01-01"}]}
""",MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8082/stocks/internal/momentum-z-stats/date"))
                .andRespond(withSuccess("""
{"value":null}
""",MediaType.APPLICATION_JSON));
        var grouped = prices.getStockPricesForTickers(List.of("TCS"), null);
        assertEquals("TCS", grouped.get("TCS").get(0).getTicker());
        assertNull(new MomentumStatsService(client).getMomentumZStatsForDate(LocalDate.of(2026,1,1),"Nifty50"));
        server.verify();
    }

    @Test
    void failedBatchDoesNotClearRuleOutputAndRetryResendsIt() {
        var persistence = new DbPersistenceService(new StockStatsService(client,new ProcessorProperties()),prices,
                new MomentumStatsService(client),new PortfolioService(new RestTemplate(), "http://localhost:8083/pf"),
                new ProcessorProperties());
        var buffer = persistence.newBuffer();
        var rows = List.of(price("A"),price("B"),price("C"));
        Map<String,Object> context = new HashMap<>();
        context.put("stockPriceList",rows);
        persistence.cacheData(buffer,context,"STOCK_PRICE");
        expectBatch("""
[{"ticker":"A"},{"ticker":"B"}]
""");
        server.expect(requestTo("http://localhost:8082/stocks/internal/stock-prices/batch"))
                .andRespond(withServerError());
        assertThrows(HttpServerErrorException.class,()->persistence.flushData(buffer,true));
        server.verify();
        server.reset();
        expectBatch("""
[{"ticker":"A"},{"ticker":"B"}]
""");
        expectBatch("""
[{"ticker":"C"}]
""");
        persistence.flushData(buffer,true);
        persistence.flushData(buffer,true); // Successful writes clear the buffer.
        server.verify();
    }

    @Test
    void plainModelsStillWorkInStoredRuleExpressions() {
        var rule = new RuleDTO();
        var item = new RuleItemDTO();
        item.setCondition("true");
        item.setThenValue("stockPriceList[0].ticker = 'INFY'");
        rule.setRuleItems(List.of(item));
        var row = price("TCS");
        new RuleEngineService().executeRule(new HashMap<>(Map.of("stockPriceList",List.of(row))),rule);
        assertEquals("INFY",row.getTicker());
    }

    private StockPrice price(String ticker) {
        var row = new StockPrice();
        row.setTicker(ticker);
        return row;
    }

    private void expectBatch(String json) {
        server.expect(requestTo("http://localhost:8082/stocks/internal/stock-prices/batch"))
                .andExpect(method(HttpMethod.POST)).andExpect(content().json(json,false))
                .andRespond(withSuccess());
    }
}
