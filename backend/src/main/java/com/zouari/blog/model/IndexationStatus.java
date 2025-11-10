package com.zouari.blog.model;

public class IndexationStatus {
    private String jobId;
    private Status status;
    private int totalPages;
    private int processedPages;
    private int totalUsers;
    private String message;
    private Long startTime;
    private Long endTime;

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public IndexationStatus() {
    }

    public IndexationStatus(String jobId, Status status) {
        this.jobId = jobId;
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getProcessedPages() {
        return processedPages;
    }

    public void setProcessedPages(int processedPages) {
        this.processedPages = processedPages;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
