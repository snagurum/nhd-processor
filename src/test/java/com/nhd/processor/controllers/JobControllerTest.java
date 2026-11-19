package com.nhd.processor.controllers;

import com.nhd.processor.jobs.services.ETLEngineService;
import com.nhd.processor.models.JobRunner;
import com.nhd.processor.services.JobRunnerService;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobControllerTest {

    @Test
    void exposesActiveJobRejectsConcurrentRequestsAndReleasesSlotAfterFailure() throws Exception {
        var jobs = mock(JobRunnerService.class);
        var etl = mock(ETLEngineService.class);
        var nightlyJob = new JobRunner();
        when(jobs.getJobRunnerByName("nightly")).thenReturn(Optional.of(nightlyJob));
        when(jobs.getJobRunnerByName("refresh")).thenReturn(Optional.of(new JobRunner()));

        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        doAnswer(invocation -> {
            started.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Test did not release running job");
            }
            throw new IllegalStateException("Job failed");
        }).when(etl).executeJobRunner(same(nightlyJob), isNull(), isNull());

        var executor = Executors.newSingleThreadExecutor();
        var controller = new JobController(jobs, etl, executor);
        try {
            assertEquals(HttpStatus.ACCEPTED, controller.jobRunner("nightly", null, null).getStatusCode());
            assertTrue(started.await(5, TimeUnit.SECONDS));

            var status = controller.jobRunnerStatus().getBody();
            assertNotNull(status);
            assertTrue(status.running());
            assertEquals("nightly", status.jobName());
            assertNotNull(status.startedAt());

            var rejected = controller.jobRunner("refresh", null, null);
            assertEquals(HttpStatus.CONFLICT, rejected.getStatusCode());
            assertEquals("currently JobRunner(nightly) is running", rejected.getBody());

            release.countDown();
            executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
            var failed = controller.jobRunnerStatus().getBody();
            assertFalse(failed.running());
            assertEquals(JobController.JobState.FAILED, failed.state());
            assertNotNull(failed.finishedAt());

            assertEquals(HttpStatus.ACCEPTED, controller.jobRunner("refresh", null, null).getStatusCode());
            executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
            var completed = controller.jobRunnerStatus().getBody();
            assertEquals(JobController.JobState.SUCCEEDED, completed.state());
            assertEquals("refresh", completed.jobName());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}
