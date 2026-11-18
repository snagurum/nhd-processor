package com.nhd.processor.re.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import com.nhd.processor.models.StockPrice;
import com.nhd.processor.models.StockStats;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleFunctionsTest {

    @Test
    void topByRanksAllRecordsWithoutChangingTheInput() {
        List<StockStats> rows = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            rows.add(StockStats.builder().return1yr(BigDecimal.valueOf(i)).build());
        }
        rows.add(null);
        rows.add(new StockStats());
        List<StockStats> original = new ArrayList<>(rows);

        List<StockStats> result = RuleFunctions.topBy(rows, "return1yr", 10);

        assertEquals(10, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertSame(rows.get(2499 - i), result.get(i));
        }
        assertEquals(original, rows);
    }

    @Test
    void topByPreservesPrecisionAndHandlesFewerMatchesAndNonpositiveLimits() {
        StockStats lower = StockStats.builder().return1yr(new BigDecimal("1.000000000000000001")).build();
        StockStats higher = StockStats.builder().return1yr(new BigDecimal("1.000000000000000002")).build();
        StockStats negative = StockStats.builder().return1yr(new BigDecimal("-2")).build();
        List<StockStats> rows = Arrays.asList(lower, null, new StockStats(), negative, higher);

        assertEquals(List.of(higher, lower, negative), RuleFunctions.topBy(rows, "return1yr", 10));
        assertTrue(RuleFunctions.topBy(rows, "return1yr", 0).isEmpty());
        assertTrue(RuleFunctions.topBy(rows, "return1yr", -1).isEmpty());
    }

    @Test
    void mapByKeepsDateKeysAndUsesTheLastRecordForDuplicateKeys() {
        LocalDate date = LocalDate.of(2026, 11, 18);
        StockPrice first = new StockPrice();
        first.setPriceDate(date);
        StockPrice last = new StockPrice();
        last.setPriceDate(date);

        Map<Object, StockPrice> result = RuleFunctions.mapBy(
                Arrays.asList(first, null, new StockPrice(), last), "priceDate");

        assertEquals(1, result.size());
        assertSame(last, result.get(date));
    }

    @Test
    void parseDateUsesEnglishMonthNamesAndHandlesNullAndInvalidInput() {
        assertEquals(LocalDate.of(2026, 11, 18), RuleFunctions.parseDate_ddMonyyyy("18-Nov-2026"));
        assertNull(RuleFunctions.parseDate_ddMonyyyy(null));
        assertThrows(DateTimeParseException.class, () -> RuleFunctions.parseDate_ddMonyyyy("invalid"));
    }

    @Test
    void ruleEngineRegistersAllFunctionsForConditionsAndActions() {
        StockStats first = StockStats.builder().return1yr(BigDecimal.ONE).build();
        StockStats best = StockStats.builder().return1yr(BigDecimal.TEN).build();
        StockPrice price = new StockPrice();
        price.setPriceDate(LocalDate.of(2026, 11, 18));
        Map<String, Object> context = new HashMap<>();
        context.put("stockStatsList", List.of(first, best));
        context.put("stockPriceList", List.of(price));
        RuleItemDTO item = new RuleItemDTO();
        item.setCondition("parseDate_ddMonyyyy('18-Nov-2026') != null");
        item.setThenValue("""
                leaders = topBy(stockStatsList, 'return1yr', 1);
                pricesByDate = mapBy(stockPriceList, 'priceDate');
                price = pricesByDate.get(parseDate_ddMonyyyy('18-Nov-2026'));
                """);
        RuleDTO rule = new RuleDTO();
        rule.setRuleItems(List.of(item));
        RuleEngineService engine = new RuleEngineService();

        engine.executeRule(context, rule);
        engine.executeRule(context, rule);

        assertEquals(List.of(best), context.get("leaders"));
        assertSame(price, context.get("price"));
    }
}
