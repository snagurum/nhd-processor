package com.nhd.processor.services;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RuleEngineService {

    public void executeRule(Map<String, Object> context, RuleDTO rule){
        for (RuleItemDTO item : rule.getRuleItems()) {
            String conditionStr = item.getCondition();
            Boolean isMatched = MVEL.eval(conditionStr, context, Boolean.class);
            if (Boolean.TRUE.equals(isMatched)) {
                executeAction(item.getThenValue(), context);
            } else if (item.getElseValue() != null) {
                executeAction(item.getElseValue(), context);
            }
        }
    }

    private void executeAction(String actionExpression, Map<String, Object> context) {
        if (actionExpression != null && !actionExpression.trim().isEmpty()) {
            MVEL.eval(actionExpression, context);
        }
    }
}
