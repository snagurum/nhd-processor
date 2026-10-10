package com.nhd.processor.controllers;

import java.util.List;

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

import com.nhd.processor.dto.RuleDTO;
import com.nhd.processor.services.RuleService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("re/api/rules")
@AllArgsConstructor
public class ReController {

    private RuleService ruleService;

    @PostMapping
    public ResponseEntity<RuleDTO> createRule(@RequestBody RuleDTO ruleDTO) {
        RuleDTO savedRule = ruleService.createRule(ruleDTO);
        return new ResponseEntity<>(savedRule, HttpStatus.CREATED);
    }

    // @GetMapping
    // public ResponseEntity<List<RuleDTO>> getAllRules() {
    //     List<RuleDTO> rules = ruleService.getAllRules();
    //     return ResponseEntity.ok(rules);
    // }

    @GetMapping
    public ResponseEntity<List<RuleDTO>> getRules(@RequestParam(required = false) String name) {
        List<RuleDTO> rules;
        
        if (name != null && !name.trim().isEmpty()) {
            rules = ruleService.getRulesByName(name);
        } else {
            rules = ruleService.getAllRules();
        }
        
        return ResponseEntity.ok(rules);
    }
    @PutMapping("/{id}")
    public ResponseEntity<RuleDTO> updateRule(@PathVariable Long id, @RequestBody RuleDTO ruleDTO) {
        RuleDTO updatedRule = ruleService.updateRule(id, ruleDTO);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
