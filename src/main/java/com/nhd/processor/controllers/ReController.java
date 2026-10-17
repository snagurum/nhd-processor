package com.nhd.processor.controllers;

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.services.RuleService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/rule")
@AllArgsConstructor
public class ReController {

    private RuleService ruleService;

    @PostMapping
    public ResponseEntity<RuleDTO> createRule(@RequestBody RuleDTO ruleDTO) {
        System.out.println("RuleDTO = "+ruleDTO);
        RuleDTO savedRule = ruleService.createRule(ruleDTO);
        return new ResponseEntity<>(savedRule, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RuleDTO>> getRules(@RequestParam(required = false) String name) {
        List<RuleDTO> rules = new ArrayList<>();
        RuleDTO rule;
        
        if (name != null && !name.trim().isEmpty()) {
            rule = ruleService.getRulesByName(name);
            rules.add(rule);
        } else {
            rules = ruleService.getAllRules();
        }
        
        return ResponseEntity.ok(rules);
    }
    @PutMapping
    public ResponseEntity<RuleDTO> updateRule( @RequestBody RuleDTO ruleDTO) {
        RuleDTO updatedRule = ruleService.updateRule( ruleDTO);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
