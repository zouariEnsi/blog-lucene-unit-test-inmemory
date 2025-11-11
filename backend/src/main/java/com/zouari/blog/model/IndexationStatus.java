package com.zouari.blog.model;

public class IndexationStatus {
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

    public IndexationStatus(Status status) {
        this.status = status;
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

    public String getDurationFormatted() {
        if (startTime != null && endTime != null) {
            long durationMs = endTime - startTime;
            long seconds = durationMs / 1000;
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;

            if (minutes > 0) {
                return minutes + "min " + remainingSeconds + "s";
            } else {
                return remainingSeconds + "s";
            }
        }
        return null;
    }
}
