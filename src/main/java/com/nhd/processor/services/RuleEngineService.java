package com.nhd.processor.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleEngineService {

    private final Map<String, Serializable> expressionCache = new ConcurrentHashMap<>();

    public void executeRule(Map<String, Object> context, RuleDTO rule) {
        if (rule == null || rule.getRuleItems() == null) {
            return;
        }

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
    }

    private void executeAction(String actionExpression, Map<String, Object> context) {
        if (actionExpression != null && !actionExpression.trim().isEmpty()) {
            Serializable compiledAction = getCompiledExpression(actionExpression);
            MVEL.executeExpression(compiledAction, context);
        }
    }

    private Serializable getCompiledExpression(String expression) {
        return expressionCache.computeIfAbsent(expression, MVEL::compileExpression);
    }
}
