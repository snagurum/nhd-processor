package com.nhd.processor.controllers;

import com.nhd.processor.jobs.services.ETLEngineService;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.models.StrategyRunner;
import com.nhd.processor.services.JobRunnerService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("runJob")
@Slf4j
public class JobController {

    private final JobRunnerService jobRunnerService;
    private final ETLEngineService eTLEngineService;
    private final Executor jobRunnerExecutor;
    private final AtomicReference<JobRunnerStatus> jobStatus = new AtomicReference<>(
        new JobRunnerStatus(false, null, null, null, JobState.IDLE));

    public JobController(JobRunnerService jobRunnerService, ETLEngineService eTLEngineService,
            @Qualifier("jobRunnerExecutor") Executor jobRunnerExecutor) {
        this.jobRunnerService = jobRunnerService;
        this.eTLEngineService = eTLEngineService;
        this.jobRunnerExecutor = jobRunnerExecutor;
    }

    @GetMapping("/jobRunner/status")
    public ResponseEntity<JobRunnerStatus> jobRunnerStatus() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(jobStatus.get());
    }

    public enum JobState { IDLE, RUNNING, SUCCEEDED, FAILED }

    public record JobRunnerStatus(boolean running, String jobName, Instant startedAt, Instant finishedAt, JobState state) {}

    @GetMapping("/jobRunner")
    public ResponseEntity<String> jobRunner(
        @RequestParam(required = true) String jobName,
        @RequestParam(required = false) List<String> tickers,
        @RequestParam(required = false) LocalDate fromDate
    ) {
        JobRunnerStatus requestedJob = new JobRunnerStatus(true, jobName, Instant.now(), null, JobState.RUNNING);
        JobRunnerStatus previous = jobStatus.getAndUpdate(current -> current.running() ? current : requestedJob);
        if (previous.running()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("currently JobRunner(" + previous.jobName() + ") is running");
        }

        boolean submitted = false;
        try {
            JobRunner jobRunner = jobRunnerService.getJobRunnerByName(jobName).orElse(null);
            if (jobRunner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("JobRunner " + jobName + " not found");
            }

            jobRunnerExecutor.execute(() -> executeJob(requestedJob, jobRunner, tickers, fromDate));
            submitted = true;
            return ResponseEntity.accepted().location(URI.create("jobRunner/status"))
                .cacheControl(CacheControl.noStore()).body("Accepted");
        } catch (RejectedExecutionException e) {
            log.warn("Could not submit JobRunner {}", jobName, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("JobRunner worker is unavailable");
        } finally {
            if (!submitted) {
                jobStatus.compareAndSet(requestedJob, previous);
            }
        }
    }

    private void executeJob(JobRunnerStatus job, JobRunner jobRunner, List<String> tickers, LocalDate fromDate) {
        long startNanos = System.nanoTime();
        JobState outcome = JobState.FAILED;
        try {
            log.info("/jobRunner({}) started", job.jobName());
            eTLEngineService.executeJobRunner(jobRunner, tickers, fromDate);
            outcome = JobState.SUCCEEDED;
        } catch (Exception e) {
            log.error("JobRunner {} failed", job.jobName(), e);
        } finally {
            jobStatus.set(new JobRunnerStatus(false, job.jobName(), job.startedAt(), Instant.now(), outcome));
            log.info("/jobRunner({}) finished in {} seconds with status {}", job.jobName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)/60000, outcome);
        }
    }

    @PostMapping("/strategyRunner")
    public ResponseEntity<String> strategyRunner(@RequestBody StrategyRunner strategyRunner) {
        log.info("/strategyRunner({}) started", strategyRunner.getName());
        long startNanos = System.nanoTime();
        try {
            if (strategyRunner.getId() == null || strategyRunner.getId() <= 0
                    || strategyRunner.getRuleName() == null || strategyRunner.getRuleName().isBlank()) {
                return ResponseEntity.badRequest().body("Strategy runner requires an id and ruleName");
            }
            if (strategyRunner.getStartDate() != null && strategyRunner.getEndDate() != null
                    && strategyRunner.getEndDate().isBefore(strategyRunner.getStartDate())) {
                return ResponseEntity.badRequest().body("endDate must be on or after startDate");
            }

            eTLEngineService.executeStrategyRunner(strategyRunner);
            return ResponseEntity.ok("OK");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Strategy execution failed for runner {}", strategyRunner.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Strategy execution failed");
        } finally {
            log.info("/strategyRunner({})  finished in {} seconds"
                , strategyRunner.getName()
                , TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)/60000
            );
        }
    }

}
