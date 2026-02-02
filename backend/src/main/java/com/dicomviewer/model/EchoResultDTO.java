package com.dicomviewer.model;

import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import java.time.Instant;

public class EchoResultDTO {
    private Long aeId;
    private String aeTitle;
    private String hostname;
    private int port;
    private ConnectionStatus status;
    private boolean success;
    private long responseTimeMs;
    private String message;
    private Instant timestamp;

    public EchoResultDTO() {}

    public EchoResultDTO(Long aeId, String aeTitle, String hostname, int port,
                         ConnectionStatus status, boolean success, long responseTimeMs,
                         String message) {
        this.aeId = aeId;
        this.aeTitle = aeTitle;
        this.hostname = hostname;
        this.port = port;
        this.status = status;
        this.success = success;
        this.responseTimeMs = responseTimeMs;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public Long getAeId() { return aeId; }
    public void setAeId(Long aeId) { this.aeId = aeId; }
    public String getAeTitle() { return aeTitle; }
    public void setAeTitle(String aeTitle) { this.aeTitle = aeTitle; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
