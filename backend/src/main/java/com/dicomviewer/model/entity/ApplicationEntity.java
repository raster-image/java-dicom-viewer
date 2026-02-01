package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * Application Entity (AE) configuration for DICOM network communication.
 * Represents both local and remote DICOM nodes.
 */
@Entity
@Table(name = "application_entities")
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 16)
    @Column(name = "ae_title", nullable = false, unique = true)
    private String aeTitle;

    @NotBlank
    @Column(nullable = false)
    private String hostname;

    @NotNull
    @Min(1)
    @Max(65535)
    @Column(nullable = false)
    private Integer port;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ae_type", nullable = false)
    private AEType aeType;

    @Column(length = 255)
    private String description;

    @Column(name = "dicomweb_url")
    private String dicomWebUrl;

    @Column(name = "query_retrieve_level")
    @Enumerated(EnumType.STRING)
    private QueryRetrieveLevel queryRetrieveLevel = QueryRetrieveLevel.STUDY;

    @Column(name = "is_default")
    private boolean defaultAE = false;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout = 30000; // ms

    @Column(name = "response_timeout")
    private Integer responseTimeout = 60000; // ms

    @Column(name = "max_associations")
    private Integer maxAssociations = 10;

    @Column(name = "last_echo_status")
    @Enumerated(EnumType.STRING)
    private ConnectionStatus lastEchoStatus;

    @Column(name = "last_echo_time")
    private Instant lastEchoTime;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Enums
    public enum AEType {
        LOCAL,           // This application's local AE
        REMOTE_LEGACY,   // Remote PACS using traditional DICOM
        REMOTE_DICOMWEB  // Remote PACS using DICOMweb
    }

    public enum QueryRetrieveLevel {
        PATIENT, STUDY, SERIES, IMAGE
    }

    public enum ConnectionStatus {
        UNKNOWN, SUCCESS, FAILED, TIMEOUT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAeTitle() { return aeTitle; }
    public void setAeTitle(String aeTitle) { this.aeTitle = aeTitle; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public AEType getAeType() { return aeType; }
    public void setAeType(AEType aeType) { this.aeType = aeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDicomWebUrl() { return dicomWebUrl; }
    public void setDicomWebUrl(String dicomWebUrl) { this.dicomWebUrl = dicomWebUrl; }

    public QueryRetrieveLevel getQueryRetrieveLevel() { return queryRetrieveLevel; }
    public void setQueryRetrieveLevel(QueryRetrieveLevel queryRetrieveLevel) { 
        this.queryRetrieveLevel = queryRetrieveLevel; 
    }

    public boolean isDefaultAE() { return defaultAE; }
    public void setDefaultAE(boolean defaultAE) { this.defaultAE = defaultAE; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Integer connectionTimeout) { 
        this.connectionTimeout = connectionTimeout; 
    }

    public Integer getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(Integer responseTimeout) { 
        this.responseTimeout = responseTimeout; 
    }

    public Integer getMaxAssociations() { return maxAssociations; }
    public void setMaxAssociations(Integer maxAssociations) { 
        this.maxAssociations = maxAssociations; 
    }

    public ConnectionStatus getLastEchoStatus() { return lastEchoStatus; }
    public void setLastEchoStatus(ConnectionStatus lastEchoStatus) { 
        this.lastEchoStatus = lastEchoStatus; 
    }

    public Instant getLastEchoTime() { return lastEchoTime; }
    public void setLastEchoTime(Instant lastEchoTime) { this.lastEchoTime = lastEchoTime; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
