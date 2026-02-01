package com.dicomviewer.model;

import java.time.Instant;

/**
 * Model for tracking C-MOVE retrieval progress.
 */
public class RetrievalProgress {

    private final String retrievalId;
    private Status status;
    private int total;
    private int completed;
    private int failed;
    private int warnings;
    private String errorMessage;
    private final Instant startTime;
    private Instant endTime;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED,
        CANCELLED
    }

    public RetrievalProgress(String retrievalId) {
        this.retrievalId = retrievalId;
        this.status = Status.PENDING;
        this.startTime = Instant.now();
    }

    // Getters and setters
    public String getRetrievalId() { return retrievalId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status;
        if (status == Status.COMPLETED || status == Status.FAILED || 
            status == Status.CANCELLED || status == Status.COMPLETED_WITH_ERRORS) {
            this.endTime = Instant.now();
        }
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public int getWarnings() { return warnings; }
    public void setWarnings(int warnings) { this.warnings = warnings; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }

    public int getPercentComplete() {
        return total > 0 ? (completed * 100) / total : 0;
    }

    public int getRemaining() {
        return Math.max(0, total - completed - failed);
    }
}
