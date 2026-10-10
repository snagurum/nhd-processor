package com.nhd.processor.services;



import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.dto.RuleItemDTO;
import com.nhd.processor.models.Rule;
import com.nhd.processor.models.RuleItem;
import com.nhd.processor.services.repository.RuleRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RuleService {

    private RuleRepository ruleRepository;

    @Transactional
    public RuleDTO createRule(RuleDTO dto) {
        Rule rule = new Rule();
        rule.setName(dto.getName());
        rule.setType(dto.getType());
        rule.setDescription(dto.getDescription());

        if (dto.getRuleItems() != null) {
            for (RuleItemDTO itemDto : dto.getRuleItems()) {
                RuleItem item = new RuleItem();
                item.setCondition(itemDto.getCondition());
                item.setThenValue(itemDto.getThenValue());
                item.setElseValue(itemDto.getElseValue());
                
                // Uses your helper method to link child to parent correctly
                rule.addRuleItem(item);
            }
        }

        Rule savedRule = ruleRepository.save(rule);
        return convertToDTO(savedRule);
    }

    @Transactional(readOnly = true)
    public List<RuleDTO> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert Entity back into a clean DTO response
    private RuleDTO convertToDTO(Rule rule) {
        RuleDTO dto = new RuleDTO();
        dto.setId(rule.getId());
        dto.setType(rule.getType());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        
        List<RuleItemDTO> itemDtos = rule.getRuleItems().stream().map(item -> {
            RuleItemDTO itemDto = new RuleItemDTO();
            itemDto.setId(item.getId());
            itemDto.setCondition(item.getCondition());
            itemDto.setThenValue(item.getThenValue());
            itemDto.setElseValue(item.getElseValue());
            return itemDto;
        }).collect(Collectors.toList());
        
        dto.setRuleItems(itemDtos);
        return dto;
    }

    @Transactional
    public RuleDTO updateRule(Long id, RuleDTO dto) {
        Rule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));

        existingRule.setName(dto.getName());
        existingRule.setDescription(dto.getDescription());

        existingRule.getRuleItems().clear();
        if (dto.getRuleItems() != null) {
            for (RuleItemDTO itemDto : dto.getRuleItems()) {
                RuleItem item = new RuleItem();
                item.setCondition(itemDto.getCondition());
                item.setThenValue(itemDto.getThenValue());
                item.setElseValue(itemDto.getElseValue());                
                existingRule.addRuleItem(item);
            }
        }

        Rule updatedRule = ruleRepository.save(existingRule);
        return convertToDTO(updatedRule);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException("Rule not found with id: " + id);
        }
        ruleRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<RuleDTO> getRulesByName(String name) {
        return ruleRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
