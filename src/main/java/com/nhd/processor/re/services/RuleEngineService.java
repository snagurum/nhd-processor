package com.nhd.processor.re.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RuleEngineService {

    private final Map<String, Serializable> expressionCache = new ConcurrentHashMap<>();
    private final ParserConfiguration parserConfig = createParserConfiguration();

    private static ParserConfiguration createParserConfiguration() {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setClassLoader(RuleEngineService.class.getClassLoader());
        try {
            configuration.addImport("topBy",
                    RuleFunctions.class.getMethod("topBy", List.class, String.class, int.class));
            configuration.addImport("mapBy",
                    RuleFunctions.class.getMethod("mapBy", List.class, String.class));
            configuration.addImport("parseDate_ddMonyyyy",
                    RuleFunctions.class.getMethod("parseDate_ddMonyyyy", String.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to register rule functions", e);
        }
        return configuration;
    }

    public void executeRule(Map<String, Object> context, RuleDTO rule) {
        if (rule == null || rule.getRuleItems() == null) {
            return;
        }

        try {
            for (RuleItemDTO item : rule.getRuleItems()) {
                String conditionStr = item.getCondition();
                Serializable compiledCondition = getCompiledExpression(conditionStr);
                Boolean isMatched = MVEL.executeExpression(compiledCondition, context, Boolean.class);
                if (Boolean.TRUE.equals(isMatched)) {
                    executeAction(item.getThenValue(), context);
                } else if (item.getElseValue() != null) {
                    executeAction(item.getElseValue(), context);
                }
            }
        } catch (Exception e) {
            log.error("Error... context = {}", context.get("stockPriceList"), e);
            throw e;
        }
    }

    private void executeAction(String actionExpression, Map<String, Object> context) {
        if (actionExpression != null && !actionExpression.trim().isEmpty()) {
            Serializable compiledAction = getCompiledExpression(actionExpression);
            MVEL.executeExpression(compiledAction, context);
        }
    }

    private Serializable getCompiledExpression(String expression) {
        return expressionCache.computeIfAbsent(expression, expr -> {
            ParserContext parserContext = new ParserContext(parserConfig);
            return MVEL.compileExpression(expr, parserContext);
        });
    }
}
