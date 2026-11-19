package com.nhd.processor.jobs.services;

import com.nhd.processor.config.ProcessorProperties;
import com.nhd.processor.controllers.JobController;
import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.models.StrategyPortfolio;
import com.nhd.processor.models.StrategyRunner;
import com.nhd.processor.models.StrategyTrade;
import com.nhd.processor.re.services.RuleEngineService;
import com.nhd.processor.services.PortfolioService;
import com.nhd.processor.services.RuleService;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StrategyRunnerWorkflowTest {

    @ParameterizedTest
    @CsvSource({"WEEKLY, 1", "MONTHLY, 5"})
    void movesScheduledSundayToNextTradingDateAndPersistsBothOutputs(String period, int value) {
        var rules = mock(RuleService.class);
        var engine = mock(RuleEngineService.class);
        var loader = mock(RuleEngineDataLoader.class);
        var portfolioService = mock(PortfolioService.class);
        var properties = new ProcessorProperties();
        var persistence = new DbPersistenceService(null, null, null, portfolioService, properties);
        var etl = new ETLEngineService(rules, engine, loader, persistence, null, properties);
        var controller = new JobController(null, etl, Runnable::run);

        LocalDate tradingDate = LocalDate.of(2025, 1, 6);
        var runner = StrategyRunner.builder().id(7L).ruleName(" rebalance ")
            .rebalancePeriod(period).rebalanceValue(value)
            .startDate(tradingDate.minusDays(2)).endDate(tradingDate.minusDays(1)).build();
        var rule = new RuleDTO();
        rule.setType("STRATEGY");
        rule.setDestinationTable("STRATEGY_PORTFOLIO, STRATEGY_TRADE");
        when(rules.findRuleByName("rebalance")).thenReturn(Optional.of(rule));
        when(loader.getDistinctTradingDates()).thenReturn(Map.of(
            tradingDate, tradingDate, tradingDate.plusDays(1), tradingDate.plusDays(1)));
        doCallRealMethod().when(loader).loadDestTable(anyMap(), eq(rule), isNull());

        var portfolio = StrategyPortfolio.builder().strategyRunnerId(runner.getId()).build();
        var trade = StrategyTrade.builder().strategyRunnerId(runner.getId()).build();
        doAnswer(invocation -> {
            Map<String, Object> context = invocation.getArgument(0);
            assertSame(runner, context.get("strategyRunner"));
            assertEquals(tradingDate, context.get("priceDate"));
            assertTrue(((List<?>) context.put("strategyPortfolioList", List.of(portfolio))).isEmpty());
            assertTrue(((List<?>) context.put("strategyTradeList", List.of(trade))).isEmpty());
            return null;
        }).when(engine).executeRule(anyMap(), eq(rule));

        assertEquals(HttpStatus.OK, controller.strategyRunner(runner).getStatusCode());
        verify(engine).executeRule(anyMap(), eq(rule));
        verify(portfolioService).savePortfolio(portfolio);
        verify(portfolioService).saveTrade(trade);
    }
}
