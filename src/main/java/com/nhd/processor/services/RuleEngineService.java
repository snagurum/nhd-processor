package com.nhd.processor.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    private final Map<String, Serializable> expressionCache = new ConcurrentHashMap<>();
    private final ParserConfiguration parserConfig;

    public RuleEngineService() {
        this.parserConfig = new ParserConfiguration();
        // Force MVEL to use the same ClassLoader that loaded your application models
        this.parserConfig.setClassLoader(this.getClass().getClassLoader());
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
