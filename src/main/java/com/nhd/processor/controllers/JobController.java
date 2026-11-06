package com.nhd.processor.controllers;

import com.nhd.processor.jobs.services.ETLEngineService;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.services.JobRunnerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("runJob")
@AllArgsConstructor
public class JobController {
	private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobRunnerService jobRunnerService;
    private final ETLEngineService eTLEngineService;

    @GetMapping("/jobRunner")
    public ResponseEntity<String> jobRunner(
        @RequestParam(required = true) String jobName,
        @RequestParam(required = false) List<String> tickers,
        @RequestParam(required = false) LocalDate fromDate
    ) {
        try {
            JobRunner jobRunner = jobRunnerService.getJobRunnerByName(jobName).orElseThrow(() -> new Exception("JobRunner " + jobName +" not found"));
            eTLEngineService.executeJobRunner(jobRunner,fromDate);
        } catch (Exception e) {
            log.error("Error Occured... ",e);
        }
        return ResponseEntity.ok("OK");
    }

}
