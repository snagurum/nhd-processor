package com.nhd.processor.services;

import com.nhd.processor.models.JobRunner;
import com.nhd.processor.models.TickerList;
import com.nhd.processor.services.repository.JobRunnerRepository;
import com.nhd.processor.services.repository.TickerListRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class JobRunnerService {

 
    private final JobRunnerRepository jobRunnerRepository;
    private final TickerListRepository tickerListRepository;

    public JobRunnerService(JobRunnerRepository jobRunnerRepository, TickerListRepository tickerListRepository) {
        this.jobRunnerRepository = jobRunnerRepository;
        this.tickerListRepository = tickerListRepository;
    }

    @Transactional
    public JobRunner createJobRunner(JobRunner jobRunner, String tickerListName) {
        if (jobRunnerRepository.existsByName(jobRunner.getName())) {
            throw new IllegalArgumentException("JobRunner with name '" + jobRunner.getName() + "' already exists.");
        }

        TickerList tickerList = tickerListRepository.findByName(tickerListName)
                .orElseThrow(() -> new IllegalArgumentException("TickerList not found with name: " + tickerListName));

        jobRunner.setTickerList(tickerList);
        return jobRunnerRepository.save(jobRunner);
    }

    public List<JobRunner> getAllJobRunners() {
        return jobRunnerRepository.findAll();
    }

    public Optional<JobRunner> getJobRunnerById(Long id) {
        return jobRunnerRepository.findById(id);
    }

    public Optional<JobRunner> getJobRunnerByName(String name) {
        return jobRunnerRepository.findByName(name);
    }

    @Transactional
    public JobRunner updateJobRunner(Long id, JobRunner updatedDetails, String targetTickerListName) {
        return jobRunnerRepository.findById(id)
                .map(existingRunner -> {
                    if (!existingRunner.getName().equals(updatedDetails.getName()) && 
                        jobRunnerRepository.existsByName(updatedDetails.getName())) {
                        throw new IllegalArgumentException("JobRunner with name '" + updatedDetails.getName() + "' already exists.");
                    }
                    
                    if (!existingRunner.getTickerList().getName().equals(targetTickerListName)) {
                        if (jobRunnerRepository.existsByTickerListName(targetTickerListName)) {
                            throw new IllegalArgumentException("TickerList '" + targetTickerListName + "' is already assigned to another runner.");
                        }
                        TickerList newTickerList = tickerListRepository.findByName(targetTickerListName)
                                .orElseThrow(() -> new IllegalArgumentException("TickerList not found: " + targetTickerListName));
                        existingRunner.setTickerList(newTickerList);
                    }
                    existingRunner.setName(updatedDetails.getName());
                    existingRunner.setDescription(updatedDetails.getDescription());
                    existingRunner.setRules(updatedDetails.getRules());
                    
                    return jobRunnerRepository.save(existingRunner);
                })
                .orElseThrow(() -> new IllegalArgumentException("JobRunner not found with id: " + id));
    }

    @Transactional
    public void deleteJobRunner(Long id) {
        if (!jobRunnerRepository.existsById(id)) {
            throw new IllegalArgumentException("JobRunner not found with id: " + id);
        }
        jobRunnerRepository.deleteById(id);
    }
}

