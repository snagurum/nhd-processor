package com.nhd.processor.controllers;

import com.nhd.processor.models.JobRunner;
import com.nhd.processor.services.JobRunnerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobrunner")
public class JobRunnerController {

    private final JobRunnerService jobRunnerService;

    public JobRunnerController(JobRunnerService jobRunnerService) {
        this.jobRunnerService = jobRunnerService;
    }

    @GetMapping
    public ResponseEntity<List<JobRunnerResponse>> getAllJobRunners() {
        List<JobRunnerResponse> result = jobRunnerService.getAllJobRunners()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobRunnerResponse> getJobRunnerById(@PathVariable Long id) {
        return jobRunnerService.getJobRunnerById(id)
                .map(jobRunner -> ResponseEntity.ok(toResponse(jobRunner)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createJobRunner(@RequestBody JobRunnerRequest request) {
        try {
            JobRunner jobRunner = toEntity(request);
            JobRunner saved = jobRunnerService.createJobRunner(
                jobRunner,
                request.tickerListName()
            );
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobRunner(@PathVariable Long id, @RequestBody JobRunnerRequest request) {
        try {
            JobRunner updated = jobRunnerService.updateJobRunner(
                id,
                toEntity(request),
                request.tickerListName()
            );
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobRunner(@PathVariable Long id) {
        try {
            jobRunnerService.deleteJobRunner(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private JobRunner toEntity(JobRunnerRequest request) {
        JobRunner jobRunner = new JobRunner();
        jobRunner.setName(request.name());
        jobRunner.setDescription(request.description());
        jobRunner.setRules(request.rules());
        return jobRunner;
    }

    private JobRunnerResponse toResponse(JobRunner jobRunner) {
        return new JobRunnerResponse(
            jobRunner.getId(),
            jobRunner.getName(),
            jobRunner.getDescription(),
            jobRunner.getTickerList().getName(),
            jobRunner.getRules()
        );
    }

    public record JobRunnerRequest(
        String name,
        String description,
        String tickerListName,
        String rules
    ) {}

    public record JobRunnerResponse(
        Long id,
        String name,
        String description,
        String tickerListName,
        String rules
    ) {}
}
