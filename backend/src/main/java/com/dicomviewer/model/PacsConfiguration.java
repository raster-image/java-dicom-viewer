package com.dicomviewer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a PACS configuration.
 * Supports both DICOMweb and Legacy (traditional DICOM) PACS systems.
 */
@Entity
@Table(name = "pacs_configuration")
public class PacsConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "ae_title", nullable = false, length = 16)
    private String aeTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "pacs_type", nullable = false)
    private PacsType pacsType;

    @Column(name = "wado_rs_url", length = 500)
    private String wadoRsUrl;

    @Column(name = "qido_rs_url", length = 500)
    private String qidoRsUrl;

    @Column(name = "stow_rs_url", length = 500)
    private String stowRsUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PacsType {
        DICOMWEB,
        LEGACY
    }

    // Constructors
    public PacsConfiguration() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public PacsType getPacsType() {
        return pacsType;
    }

    public void setPacsType(PacsType pacsType) {
        this.pacsType = pacsType;
    }

    public String getWadoRsUrl() {
        return wadoRsUrl;
    }

    public void setWadoRsUrl(String wadoRsUrl) {
        this.wadoRsUrl = wadoRsUrl;
    }

    public String getQidoRsUrl() {
        return qidoRsUrl;
    }

    public void setQidoRsUrl(String qidoRsUrl) {
        this.qidoRsUrl = qidoRsUrl;
    }

    public String getStowRsUrl() {
        return stowRsUrl;
    }

    public void setStowRsUrl(String stowRsUrl) {
        this.stowRsUrl = stowRsUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Check if this is a DICOMweb PACS.
     */
    public boolean isDicomWeb() {
        return pacsType == PacsType.DICOMWEB;
    }

    /**
     * Check if this is a Legacy PACS (traditional DICOM).
     */
    public boolean isLegacy() {
        return pacsType == PacsType.LEGACY;
    }
}
