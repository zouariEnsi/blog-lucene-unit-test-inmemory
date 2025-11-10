package com.zouari.blog.service;

import com.zouari.blog.model.IndexationStatus;
import com.zouari.blog.model.RandomUserResponse;
import com.zouari.blog.model.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@ApplicationScoped
public class IndexationService {
    private static final Logger LOGGER = Logger.getLogger(IndexationService.class.getName());
    private static final int TOTAL_PAGES = 50;
    private static final int RESULTS_PER_PAGE = 100;
    
    private final Map<String, IndexationStatus> jobStatuses = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    @Inject
    private RandomUserClient randomUserClient;
    
    @Inject
    private LuceneIndexService luceneIndexService;

    public String startIndexation() {
        String jobId = UUID.randomUUID().toString();
        IndexationStatus status = new IndexationStatus(jobId, IndexationStatus.Status.IN_PROGRESS);
        status.setTotalPages(TOTAL_PAGES);
        status.setProcessedPages(0);
        status.setTotalUsers(0);
        status.setStartTime(System.currentTimeMillis());
        status.setMessage("Indexation started");
        
        jobStatuses.put(jobId, status);
        
        // Submit async task
        executorService.submit(() -> performIndexation(jobId));
        
        LOGGER.info("Started indexation job: " + jobId);
        return jobId;
    }

    public IndexationStatus getStatus(String jobId) {
        return jobStatuses.getOrDefault(jobId, 
            new IndexationStatus(jobId, IndexationStatus.Status.NOT_STARTED));
    }

    private void performIndexation(String jobId) {
        IndexationStatus status = jobStatuses.get(jobId);
        List<User> allUsers = new ArrayList<>();
        
        try {
            // Clear existing index before starting
            luceneIndexService.clearIndex();
            
            // Fetch users from all pages
            for (int page = 1; page <= TOTAL_PAGES; page++) {
                try {
                    LOGGER.info("Fetching page " + page + " of " + TOTAL_PAGES);
                    RandomUserResponse response = randomUserClient.fetchUsers(page, RESULTS_PER_PAGE);
                    
                    if (response != null && response.getResults() != null) {
                        allUsers.addAll(response.getResults());
                        
                        // Update status
                        status.setProcessedPages(page);
                        status.setTotalUsers(allUsers.size());
                        status.setMessage("Processing page " + page + " of " + TOTAL_PAGES);
                    }
                    
                    // Small delay to avoid overwhelming the API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    LOGGER.warning("Error fetching page " + page + ": " + e.getMessage());
                    status.setMessage("Warning: Error fetching page " + page + " - " + e.getMessage());
                }
            }
            
            // Index all users
            LOGGER.info("Indexing " + allUsers.size() + " users");
            status.setMessage("Indexing " + allUsers.size() + " users");
            luceneIndexService.indexUsers(allUsers);
            
            // Mark as completed
            status.setStatus(IndexationStatus.Status.COMPLETED);
            status.setEndTime(System.currentTimeMillis());
            status.setMessage("Indexation completed successfully. Total users indexed: " + allUsers.size());
            LOGGER.info("Indexation job " + jobId + " completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Indexation job " + jobId + " failed: " + e.getMessage());
            status.setStatus(IndexationStatus.Status.FAILED);
            status.setEndTime(System.currentTimeMillis());
            status.setMessage("Indexation failed: " + e.getMessage());
        }
    }
}
