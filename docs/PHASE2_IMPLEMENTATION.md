# Phase 2: Legacy PACS Support - Detailed Implementation Guide

This document provides detailed technical specifications, step-by-step implementation tasks, acceptance criteria, and code examples for Phase 2 (Legacy PACS Support) of the Java DICOM Viewer project.

> **Duration**: 3-4 weeks (Weeks 7-10)  
> **Focus**: Traditional DICOM network operations and legacy PACS connectivity  
> **Prerequisites**: Phase 1 completion, dcm4che 5.x libraries, DICOM network knowledge

---

## Table of Contents

1. [Overview](#overview)
2. [Week 7-8: DICOM Network Foundation](#week-7-8-dicom-network-foundation)
3. [Week 8-9: Query Operations (C-FIND)](#week-8-9-query-operations-c-find)
4. [Week 9-10: Retrieval Operations (C-MOVE/C-STORE)](#week-9-10-retrieval-operations-c-movec-store)
5. [Acceptance Criteria](#acceptance-criteria)
6. [Testing Strategy](#testing-strategy)
7. [Deliverables Checklist](#deliverables-checklist)

---

## Overview

### Phase 2 Goals

1. **Implement traditional DICOM network operations** - Enable C-ECHO, C-FIND, C-MOVE, and C-STORE operations using dcm4che
2. **Enable connectivity to older PACS systems** - Support legacy systems that do not implement DICOMweb
3. **Provide unified query interface** - Abstract query operations across both DICOMweb and traditional DICOM protocols
4. **Implement local storage management** - Handle received DICOM files and metadata extraction

### DICOM Network Concepts

| Operation | Purpose | Role | Description |
|-----------|---------|------|-------------|
| C-ECHO | Connectivity | SCU | Verify network connection to remote AE |
| C-FIND | Query | SCU | Search for studies/series/instances on remote PACS |
| C-MOVE | Retrieve | SCU | Request remote PACS to send images to local AE |
| C-STORE | Store | SCP | Receive and store DICOM instances |

### Success Metrics

| Metric | Target |
|--------|--------|
| C-ECHO response time | < 500ms |
| C-FIND query (100 results) | < 3 seconds |
| C-MOVE initiation | < 1 second |
| C-STORE processing | < 100ms per instance |
| Connection pool efficiency | > 90% reuse |
| Test coverage | > 75% |

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Java DICOM Viewer Backend                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────┐    ┌─────────────────────┐    ┌──────────────────┐ │
│  │   REST Controllers  │    │   Unified Services  │    │  DICOM Services  │ │
│  │                     │    │                     │    │                  │ │
│  │ - AEController      │───▶│ - UnifiedQuery      │───▶│ - CechoScu       │ │
│  │ - QueryController   │    │   Service           │    │ - CfindScu       │ │
│  │ - RetrieveController│    │ - UnifiedRetrieve   │    │ - CmoveScu       │ │
│  │                     │    │   Service           │    │ - CstoreScp      │ │
│  └─────────────────────┘    └─────────────────────┘    └──────────────────┘ │
│                                      │                          │           │
│                                      ▼                          ▼           │
│                            ┌─────────────────────┐    ┌──────────────────┐ │
│                            │  Configuration      │    │  Storage Layer   │ │
│                            │                     │    │                  │ │
│                            │ - AE Configuration  │    │ - File Storage   │ │
│                            │ - Connection Pool   │    │ - Metadata DB    │ │
│                            │ - PACS Registry     │    │ - Cache Manager  │ │
│                            └─────────────────────┘    └──────────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
            ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
            │  Legacy PACS │   │  DICOMweb    │   │   Orthanc    │
            │   (C-FIND/   │   │    PACS      │   │  (Test PACS) │
            │   C-MOVE)    │   │              │   │              │
            └──────────────┘   └──────────────┘   └──────────────┘
```

---

## Week 7-8: DICOM Network Foundation

### 1. Application Entity Configuration

#### 1.1 AE Configuration Model

**Tasks:**
- [ ] Create AE configuration entity with all required fields
- [ ] Implement AE type enumeration (LOCAL, REMOTE_LEGACY, REMOTE_DICOMWEB)
- [ ] Add database schema for AE persistence
- [ ] Create AE repository with CRUD operations

**File: `model/entity/ApplicationEntity.java`**
```java
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
```

---

#### 1.2 AE Repository

**File: `repository/ApplicationEntityRepository.java`**
```java
package com.dicomviewer.repository;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationEntityRepository extends JpaRepository<ApplicationEntity, Long> {

    Optional<ApplicationEntity> findByAeTitle(String aeTitle);

    List<ApplicationEntity> findByAeType(AEType aeType);

    List<ApplicationEntity> findByEnabledTrue();

    @Query("SELECT ae FROM ApplicationEntity ae WHERE ae.aeType = 'LOCAL' AND ae.enabled = true")
    Optional<ApplicationEntity> findLocalAE();

    @Query("SELECT ae FROM ApplicationEntity ae WHERE ae.defaultAE = true AND ae.enabled = true")
    Optional<ApplicationEntity> findDefaultAE();

    List<ApplicationEntity> findByAeTypeAndEnabledTrue(AEType aeType);

    boolean existsByAeTitle(String aeTitle);
}
```

---

#### 1.3 AE Management Service

**File: `service/ApplicationEntityService.java`**
```java
package com.dicomviewer.service;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import com.dicomviewer.repository.ApplicationEntityRepository;
import com.dicomviewer.dicom.network.CechoScu;
import com.dicomviewer.exception.AENotFoundException;
import com.dicomviewer.exception.DuplicateAETitleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Application Entity configurations.
 */
@Service
@Transactional
public class ApplicationEntityService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEntityService.class);

    private final ApplicationEntityRepository aeRepository;
    private final CechoScu cechoScu;

    public ApplicationEntityService(ApplicationEntityRepository aeRepository, CechoScu cechoScu) {
        this.aeRepository = aeRepository;
        this.cechoScu = cechoScu;
    }

    /**
     * Create a new Application Entity configuration.
     */
    public ApplicationEntity createAE(ApplicationEntity ae) {
        if (aeRepository.existsByAeTitle(ae.getAeTitle())) {
            throw new DuplicateAETitleException(
                "AE Title '" + ae.getAeTitle() + "' already exists"
            );
        }

        // If this is marked as default, unset any existing default
        if (ae.isDefaultAE()) {
            aeRepository.findDefaultAE().ifPresent(existing -> {
                existing.setDefaultAE(false);
                aeRepository.save(existing);
            });
        }

        log.info("Creating new AE configuration: {}", ae.getAeTitle());
        return aeRepository.save(ae);
    }

    /**
     * Update an existing Application Entity configuration.
     */
    public ApplicationEntity updateAE(Long id, ApplicationEntity updates) {
        ApplicationEntity existing = aeRepository.findById(id)
            .orElseThrow(() -> new AENotFoundException("AE with id " + id + " not found"));

        // Check for AE title conflicts if title is being changed
        if (!existing.getAeTitle().equals(updates.getAeTitle()) &&
            aeRepository.existsByAeTitle(updates.getAeTitle())) {
            throw new DuplicateAETitleException(
                "AE Title '" + updates.getAeTitle() + "' already exists"
            );
        }

        // Update fields
        existing.setAeTitle(updates.getAeTitle());
        existing.setHostname(updates.getHostname());
        existing.setPort(updates.getPort());
        existing.setAeType(updates.getAeType());
        existing.setDescription(updates.getDescription());
        existing.setDicomWebUrl(updates.getDicomWebUrl());
        existing.setQueryRetrieveLevel(updates.getQueryRetrieveLevel());
        existing.setEnabled(updates.isEnabled());
        existing.setConnectionTimeout(updates.getConnectionTimeout());
        existing.setResponseTimeout(updates.getResponseTimeout());
        existing.setMaxAssociations(updates.getMaxAssociations());

        // Handle default flag
        if (updates.isDefaultAE() && !existing.isDefaultAE()) {
            aeRepository.findDefaultAE().ifPresent(other -> {
                other.setDefaultAE(false);
                aeRepository.save(other);
            });
        }
        existing.setDefaultAE(updates.isDefaultAE());

        log.info("Updated AE configuration: {}", existing.getAeTitle());
        return aeRepository.save(existing);
    }

    /**
     * Delete an Application Entity configuration.
     */
    public void deleteAE(Long id) {
        ApplicationEntity ae = aeRepository.findById(id)
            .orElseThrow(() -> new AENotFoundException("AE with id " + id + " not found"));

        if (ae.getAeType() == AEType.LOCAL) {
            throw new IllegalStateException("Cannot delete local AE configuration");
        }

        log.info("Deleting AE configuration: {}", ae.getAeTitle());
        aeRepository.delete(ae);
    }

    /**
     * Get all Application Entities.
     */
    @Transactional(readOnly = true)
    public List<ApplicationEntity> getAllAEs() {
        return aeRepository.findAll();
    }

    /**
     * Get all enabled remote AEs for querying.
     */
    @Transactional(readOnly = true)
    public List<ApplicationEntity> getEnabledRemoteAEs() {
        return aeRepository.findByEnabledTrue().stream()
            .filter(ae -> ae.getAeType() != AEType.LOCAL)
            .toList();
    }

    /**
     * Get AE by ID.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationEntity> getAEById(Long id) {
        return aeRepository.findById(id);
    }

    /**
     * Get AE by title.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationEntity> getAEByTitle(String aeTitle) {
        return aeRepository.findByAeTitle(aeTitle);
    }

    /**
     * Get the local AE configuration.
     */
    @Transactional(readOnly = true)
    public ApplicationEntity getLocalAE() {
        return aeRepository.findLocalAE()
            .orElseThrow(() -> new IllegalStateException("Local AE not configured"));
    }

    /**
     * Test connectivity to a remote AE using C-ECHO.
     */
    public ConnectionStatus testConnection(Long aeId) {
        ApplicationEntity ae = aeRepository.findById(aeId)
            .orElseThrow(() -> new AENotFoundException("AE with id " + aeId + " not found"));

        if (ae.getAeType() == AEType.LOCAL) {
            throw new IllegalArgumentException("Cannot test connection to local AE");
        }

        log.info("Testing connection to AE: {} at {}:{}", 
            ae.getAeTitle(), ae.getHostname(), ae.getPort());

        ConnectionStatus status;
        try {
            ApplicationEntity localAE = getLocalAE();
            boolean success = cechoScu.echo(localAE, ae);
            status = success ? ConnectionStatus.SUCCESS : ConnectionStatus.FAILED;
        } catch (Exception e) {
            log.error("C-ECHO failed to {}: {}", ae.getAeTitle(), e.getMessage());
            status = ConnectionStatus.FAILED;
        }

        // Update AE with echo status
        ae.setLastEchoStatus(status);
        ae.setLastEchoTime(Instant.now());
        aeRepository.save(ae);

        return status;
    }

    /**
     * Test connectivity to all enabled remote AEs.
     */
    public void testAllConnections() {
        List<ApplicationEntity> remoteAEs = getEnabledRemoteAEs();
        for (ApplicationEntity ae : remoteAEs) {
            if (ae.getAeType() == AEType.REMOTE_LEGACY) {
                testConnection(ae.getId());
            }
        }
    }
}
```

---

#### 1.4 AE Controller (REST API)

**File: `controller/ApplicationEntityController.java`**
```java
package com.dicomviewer.controller;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import com.dicomviewer.service.ApplicationEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ae")
@Tag(name = "Application Entities", description = "DICOM Application Entity management")
public class ApplicationEntityController {

    private final ApplicationEntityService aeService;

    public ApplicationEntityController(ApplicationEntityService aeService) {
        this.aeService = aeService;
    }

    @GetMapping
    @Operation(summary = "List all Application Entities")
    public ResponseEntity<List<ApplicationEntity>> getAllAEs() {
        return ResponseEntity.ok(aeService.getAllAEs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Application Entity by ID")
    public ResponseEntity<ApplicationEntity> getAE(
            @Parameter(description = "AE ID") @PathVariable Long id) {
        return aeService.getAEById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new Application Entity")
    public ResponseEntity<ApplicationEntity> createAE(
            @Valid @RequestBody ApplicationEntity ae) {
        ApplicationEntity created = aeService.createAE(ae);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Application Entity")
    public ResponseEntity<ApplicationEntity> updateAE(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationEntity ae) {
        ApplicationEntity updated = aeService.updateAE(id, ae);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Application Entity")
    public ResponseEntity<Void> deleteAE(@PathVariable Long id) {
        aeService.deleteAE(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test connectivity to remote AE using C-ECHO")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long id) {
        ConnectionStatus status = aeService.testConnection(id);
        return ResponseEntity.ok(Map.of(
            "aeId", id,
            "status", status.name(),
            "success", status == ConnectionStatus.SUCCESS
        ));
    }

    @PostMapping("/test-all")
    @Operation(summary = "Test connectivity to all enabled remote AEs")
    public ResponseEntity<Map<String, String>> testAllConnections() {
        aeService.testAllConnections();
        return ResponseEntity.ok(Map.of("message", "Connection tests initiated"));
    }
}
```

---

### 2. C-ECHO Implementation

#### 2.1 C-ECHO SCU Service

**Tasks:**
- [ ] Implement C-ECHO Service Class User (SCU) using dcm4che
- [ ] Create connection management with configurable timeouts
- [ ] Implement connection pooling for efficiency
- [ ] Add comprehensive error handling and logging

**File: `dicom/network/CechoScu.java`**
```java
package com.dicomviewer.dicom.network;

import com.dicomviewer.model.entity.ApplicationEntity;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * C-ECHO Service Class User (SCU) implementation.
 * Used for verifying DICOM network connectivity.
 */
@Component
public class CechoScu {

    private static final Logger log = LoggerFactory.getLogger(CechoScu.class);

    private final Device device;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    public CechoScu() {
        this.device = new Device("DICOMVIEWER_ECHO");
        this.executor = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.device.setExecutor(executor);
        this.device.setScheduledExecutor(scheduledExecutor);
    }

    /**
     * Perform C-ECHO to verify connectivity to remote AE.
     *
     * @param localAE  Local Application Entity configuration
     * @param remoteAE Remote Application Entity to test
     * @return true if echo successful, false otherwise
     */
    public boolean echo(ApplicationEntity localAE, ApplicationEntity remoteAE) {
        log.info("Performing C-ECHO from {} to {} ({}:{})",
            localAE.getAeTitle(), remoteAE.getAeTitle(),
            remoteAE.getHostname(), remoteAE.getPort());

        ApplicationEntity ae = createApplicationEntity(localAE);
        Connection localConn = createConnection(localAE);
        Connection remoteConn = createRemoteConnection(remoteAE);

        ae.addConnection(localConn);
        device.addApplicationEntity(ae);
        device.addConnection(localConn);

        Association association = null;
        try {
            AAssociateRQ rq = createAssociateRequest(localAE, remoteAE);
            association = ae.connect(remoteConn, rq);

            DimseRSP response = association.cecho();
            response.next();

            int status = response.getCommand().getInt(org.dcm4che3.data.Tag.Status, -1);
            boolean success = status == 0;

            log.info("C-ECHO to {} completed with status: {} ({})",
                remoteAE.getAeTitle(), status, success ? "SUCCESS" : "FAILED");

            return success;

        } catch (IOException | InterruptedException | IncompatibleConnectionException |
                 GeneralSecurityException e) {
            log.error("C-ECHO to {} failed: {}", remoteAE.getAeTitle(), e.getMessage());
            return false;
        } finally {
            if (association != null && association.isReadyForDataTransfer()) {
                try {
                    association.release();
                } catch (IOException e) {
                    log.warn("Error releasing association: {}", e.getMessage());
                }
            }
            device.removeApplicationEntity(ae.getAETitle());
            device.removeConnection(localConn);
        }
    }

    private ApplicationEntity createApplicationEntity(ApplicationEntity config) {
        ApplicationEntity ae = new ApplicationEntity(config.getAeTitle());
        ae.setAssociationAcceptor(false);
        ae.setAssociationInitiator(true);
        return ae;
    }

    private Connection createConnection(ApplicationEntity config) {
        Connection conn = new Connection();
        conn.setConnectTimeout(config.getConnectionTimeout());
        conn.setRequestTimeout(config.getResponseTimeout());
        return conn;
    }

    private Connection createRemoteConnection(ApplicationEntity remoteAE) {
        Connection conn = new Connection();
        conn.setHostname(remoteAE.getHostname());
        conn.setPort(remoteAE.getPort());
        return conn;
    }

    private AAssociateRQ createAssociateRequest(ApplicationEntity localAE, 
                                                 ApplicationEntity remoteAE) {
        AAssociateRQ rq = new AAssociateRQ();
        rq.setCallingAET(localAE.getAeTitle());
        rq.setCalledAET(remoteAE.getAeTitle());
        rq.addPresentationContext(new PresentationContext(
            1,
            UID.Verification,
            UID.ImplicitVRLittleEndian
        ));
        return rq;
    }

    /**
     * Cleanup resources on shutdown.
     */
    public void shutdown() {
        executor.shutdown();
        scheduledExecutor.shutdown();
    }
}
```

---

### 3. Frontend - PACS Configuration UI

#### 3.1 PACS Configuration Types

**File: `frontend/src/types/pacs.ts`**
```typescript
/**
 * Application Entity configuration
 */
export interface ApplicationEntity {
  id: number;
  aeTitle: string;
  hostname: string;
  port: number;
  aeType: AEType;
  description?: string;
  dicomWebUrl?: string;
  queryRetrieveLevel: QueryRetrieveLevel;
  defaultAE: boolean;
  enabled: boolean;
  connectionTimeout: number;
  responseTimeout: number;
  maxAssociations: number;
  lastEchoStatus?: ConnectionStatus;
  lastEchoTime?: string;
  createdAt: string;
  updatedAt: string;
}

export type AEType = 'LOCAL' | 'REMOTE_LEGACY' | 'REMOTE_DICOMWEB';
export type QueryRetrieveLevel = 'PATIENT' | 'STUDY' | 'SERIES' | 'IMAGE';
export type ConnectionStatus = 'UNKNOWN' | 'SUCCESS' | 'FAILED' | 'TIMEOUT';

/**
 * Create/Update AE request
 */
export interface AERequest {
  aeTitle: string;
  hostname: string;
  port: number;
  aeType: AEType;
  description?: string;
  dicomWebUrl?: string;
  queryRetrieveLevel?: QueryRetrieveLevel;
  defaultAE?: boolean;
  enabled?: boolean;
  connectionTimeout?: number;
  responseTimeout?: number;
  maxAssociations?: number;
}

/**
 * Connection test result
 */
export interface ConnectionTestResult {
  aeId: number;
  status: ConnectionStatus;
  success: boolean;
}
```

---

#### 3.2 PACS Service

**File: `frontend/src/services/pacsService.ts`**
```typescript
import { api } from './api';
import type { 
  ApplicationEntity, 
  AERequest, 
  ConnectionTestResult 
} from '../types/pacs';

export const pacsService = {
  /**
   * Get all configured Application Entities
   */
  async getAllAEs(): Promise<ApplicationEntity[]> {
    const response = await api.get<ApplicationEntity[]>('/ae');
    return response.data;
  },

  /**
   * Get a specific Application Entity by ID
   */
  async getAE(id: number): Promise<ApplicationEntity> {
    const response = await api.get<ApplicationEntity>(`/ae/${id}`);
    return response.data;
  },

  /**
   * Create a new Application Entity
   */
  async createAE(ae: AERequest): Promise<ApplicationEntity> {
    const response = await api.post<ApplicationEntity>('/ae', ae);
    return response.data;
  },

  /**
   * Update an existing Application Entity
   */
  async updateAE(id: number, ae: AERequest): Promise<ApplicationEntity> {
    const response = await api.put<ApplicationEntity>(`/ae/${id}`, ae);
    return response.data;
  },

  /**
   * Delete an Application Entity
   */
  async deleteAE(id: number): Promise<void> {
    await api.delete(`/ae/${id}`);
  },

  /**
   * Test connectivity to a remote AE using C-ECHO
   */
  async testConnection(id: number): Promise<ConnectionTestResult> {
    const response = await api.post<ConnectionTestResult>(`/ae/${id}/test`);
    return response.data;
  },

  /**
   * Test connectivity to all enabled remote AEs
   */
  async testAllConnections(): Promise<void> {
    await api.post('/ae/test-all');
  },
};
```

---

#### 3.3 PACS Configuration Component

**File: `frontend/src/components/PACSConfiguration.tsx`**
```typescript
import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { pacsService } from '../services/pacsService';
import type { ApplicationEntity, AERequest, AEType, ConnectionStatus } from '../types/pacs';

interface PACSConfigurationProps {
  onClose?: () => void;
}

export const PACSConfiguration: React.FC<PACSConfigurationProps> = ({ onClose }) => {
  const queryClient = useQueryClient();
  const [editingAE, setEditingAE] = useState<ApplicationEntity | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  // Fetch all AEs
  const { data: aeList, isLoading, error } = useQuery({
    queryKey: ['applicationEntities'],
    queryFn: pacsService.getAllAEs,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: pacsService.createAE,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicationEntities'] });
      setIsCreating(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AERequest }) =>
      pacsService.updateAE(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicationEntities'] });
      setEditingAE(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: pacsService.deleteAE,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicationEntities'] });
    },
  });

  const testMutation = useMutation({
    mutationFn: pacsService.testConnection,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicationEntities'] });
    },
  });

  const getStatusColor = (status?: ConnectionStatus): string => {
    switch (status) {
      case 'SUCCESS':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'TIMEOUT':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status?: ConnectionStatus): string => {
    switch (status) {
      case 'SUCCESS':
        return '✓';
      case 'FAILED':
        return '✗';
      case 'TIMEOUT':
        return '⏱';
      default:
        return '?';
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-100 text-red-700 rounded">
        Failed to load PACS configuration: {(error as Error).message}
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 bg-gray-50 border-b flex justify-between items-center">
        <h2 className="text-xl font-semibold text-gray-900">PACS Configuration</h2>
        <div className="flex gap-2">
          <button
            onClick={() => setIsCreating(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Add PACS
          </button>
          {onClose && (
            <button
              onClick={onClose}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              Close
            </button>
          )}
        </div>
      </div>

      {/* AE List */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">AE Title</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Host</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Port</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Type</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Status</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {aeList?.map((ae) => (
              <tr key={ae.id} className={!ae.enabled ? 'opacity-50' : ''}>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <span className="font-medium">{ae.aeTitle}</span>
                    {ae.defaultAE && (
                      <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded">
                        Default
                      </span>
                    )}
                  </div>
                  {ae.description && (
                    <div className="text-sm text-gray-500">{ae.description}</div>
                  )}
                </td>
                <td className="px-4 py-3 text-sm">{ae.hostname}</td>
                <td className="px-4 py-3 text-sm">{ae.port}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 text-xs rounded ${
                    ae.aeType === 'LOCAL' ? 'bg-purple-100 text-purple-700' :
                    ae.aeType === 'REMOTE_LEGACY' ? 'bg-orange-100 text-orange-700' :
                    'bg-green-100 text-green-700'
                  }`}>
                    {ae.aeType}
                  </span>
                </td>
                <td className="px-4 py-3">
                  {ae.aeType !== 'LOCAL' && (
                    <span className={`inline-flex items-center px-2 py-1 text-xs rounded ${getStatusColor(ae.lastEchoStatus)}`}>
                      <span className="mr-1">{getStatusIcon(ae.lastEchoStatus)}</span>
                      {ae.lastEchoStatus || 'Unknown'}
                    </span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {ae.aeType !== 'LOCAL' && (
                      <button
                        onClick={() => testMutation.mutate(ae.id)}
                        disabled={testMutation.isPending}
                        className="px-2 py-1 text-sm bg-green-100 text-green-700 rounded hover:bg-green-200"
                        title="Test Connection"
                      >
                        {testMutation.isPending ? '...' : 'Test'}
                      </button>
                    )}
                    <button
                      onClick={() => setEditingAE(ae)}
                      className="px-2 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                    >
                      Edit
                    </button>
                    {ae.aeType !== 'LOCAL' && (
                      <button
                        onClick={() => {
                          if (confirm('Are you sure you want to delete this PACS configuration?')) {
                            deleteMutation.mutate(ae.id);
                          }
                        }}
                        className="px-2 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create/Edit Modal */}
      {(isCreating || editingAE) && (
        <AEFormModal
          ae={editingAE}
          onSave={(data) => {
            if (editingAE) {
              updateMutation.mutate({ id: editingAE.id, data });
            } else {
              createMutation.mutate(data);
            }
          }}
          onCancel={() => {
            setIsCreating(false);
            setEditingAE(null);
          }}
          isLoading={createMutation.isPending || updateMutation.isPending}
        />
      )}
    </div>
  );
};

// AE Form Modal Component
interface AEFormModalProps {
  ae: ApplicationEntity | null;
  onSave: (data: AERequest) => void;
  onCancel: () => void;
  isLoading: boolean;
}

const AEFormModal: React.FC<AEFormModalProps> = ({ ae, onSave, onCancel, isLoading }) => {
  const [formData, setFormData] = useState<AERequest>({
    aeTitle: ae?.aeTitle || '',
    hostname: ae?.hostname || '',
    port: ae?.port || 11112,
    aeType: ae?.aeType || 'REMOTE_LEGACY',
    description: ae?.description || '',
    dicomWebUrl: ae?.dicomWebUrl || '',
    queryRetrieveLevel: ae?.queryRetrieveLevel || 'STUDY',
    defaultAE: ae?.defaultAE || false,
    enabled: ae?.enabled ?? true,
    connectionTimeout: ae?.connectionTimeout || 30000,
    responseTimeout: ae?.responseTimeout || 60000,
    maxAssociations: ae?.maxAssociations || 10,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  const updateField = <K extends keyof AERequest>(field: K, value: AERequest[K]) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="px-6 py-4 border-b">
          <h3 className="text-lg font-semibold">
            {ae ? 'Edit PACS Configuration' : 'Add New PACS'}
          </h3>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* AE Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              AE Title *
            </label>
            <input
              type="text"
              required
              maxLength={16}
              value={formData.aeTitle}
              onChange={(e) => updateField('aeTitle', e.target.value.toUpperCase())}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., PACS_SERVER"
            />
          </div>

          {/* Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type *
            </label>
            <select
              value={formData.aeType}
              onChange={(e) => updateField('aeType', e.target.value as AEType)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              disabled={ae?.aeType === 'LOCAL'}
            >
              <option value="REMOTE_LEGACY">Legacy DICOM</option>
              <option value="REMOTE_DICOMWEB">DICOMweb</option>
              {ae?.aeType === 'LOCAL' && <option value="LOCAL">Local</option>}
            </select>
          </div>

          {/* Hostname */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Hostname *
            </label>
            <input
              type="text"
              required
              value={formData.hostname}
              onChange={(e) => updateField('hostname', e.target.value)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., pacs.hospital.org"
            />
          </div>

          {/* Port */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Port *
            </label>
            <input
              type="number"
              required
              min={1}
              max={65535}
              value={formData.port}
              onChange={(e) => updateField('port', parseInt(e.target.value))}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* DICOMweb URL (only for DICOMweb type) */}
          {formData.aeType === 'REMOTE_DICOMWEB' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                DICOMweb URL
              </label>
              <input
                type="url"
                value={formData.dicomWebUrl}
                onChange={(e) => updateField('dicomWebUrl', e.target.value)}
                className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., https://pacs.hospital.org/dicom-web"
              />
            </div>
          )}

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <input
              type="text"
              value={formData.description}
              onChange={(e) => updateField('description', e.target.value)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Main Hospital PACS"
            />
          </div>

          {/* Advanced Settings */}
          <details className="border rounded p-3">
            <summary className="cursor-pointer text-sm font-medium text-gray-700">
              Advanced Settings
            </summary>
            <div className="mt-3 space-y-3">
              {/* Connection Timeout */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Connection Timeout (ms)
                </label>
                <input
                  type="number"
                  min={1000}
                  value={formData.connectionTimeout}
                  onChange={(e) => updateField('connectionTimeout', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Response Timeout */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Response Timeout (ms)
                </label>
                <input
                  type="number"
                  min={1000}
                  value={formData.responseTimeout}
                  onChange={(e) => updateField('responseTimeout', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Max Associations */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Max Associations
                </label>
                <input
                  type="number"
                  min={1}
                  max={50}
                  value={formData.maxAssociations}
                  onChange={(e) => updateField('maxAssociations', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </details>

          {/* Checkboxes */}
          <div className="flex gap-6">
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={formData.enabled}
                onChange={(e) => updateField('enabled', e.target.checked)}
                className="rounded"
              />
              <span className="text-sm text-gray-700">Enabled</span>
            </label>
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={formData.defaultAE}
                onChange={(e) => updateField('defaultAE', e.target.checked)}
                className="rounded"
              />
              <span className="text-sm text-gray-700">Default PACS</span>
            </label>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {isLoading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
```

---

## Week 8-9: Query Operations (C-FIND)

### 1. C-FIND SCU Implementation

#### 1.1 Query Attribute Builder

**Tasks:**
- [ ] Create query attribute builder for different query levels
- [ ] Implement Patient Root and Study Root query models
- [ ] Support wildcard matching for string attributes
- [ ] Handle date range queries

**File: `dicom/network/QueryAttributeBuilder.java`**
```java
package com.dicomviewer.dicom.network;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Builder for constructing DICOM query attributes for C-FIND operations.
 */
public class QueryAttributeBuilder {

    private static final DateTimeFormatter DICOM_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Attributes attrs;

    private QueryAttributeBuilder() {
        this.attrs = new Attributes();
    }

    public static QueryAttributeBuilder create() {
        return new QueryAttributeBuilder();
    }

    /**
     * Build attributes for Study-level query.
     */
    public QueryAttributeBuilder forStudyLevel() {
        // Query/Retrieve Level
        attrs.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        
        // Return keys - Patient level
        attrs.setNull(Tag.PatientName, VR.PN);
        attrs.setNull(Tag.PatientID, VR.LO);
        attrs.setNull(Tag.PatientBirthDate, VR.DA);
        attrs.setNull(Tag.PatientSex, VR.CS);
        
        // Return keys - Study level
        attrs.setNull(Tag.StudyInstanceUID, VR.UI);
        attrs.setNull(Tag.StudyDate, VR.DA);
        attrs.setNull(Tag.StudyTime, VR.TM);
        attrs.setNull(Tag.StudyDescription, VR.LO);
        attrs.setNull(Tag.AccessionNumber, VR.SH);
        attrs.setNull(Tag.ModalitiesInStudy, VR.CS);
        attrs.setNull(Tag.NumberOfStudyRelatedSeries, VR.IS);
        attrs.setNull(Tag.NumberOfStudyRelatedInstances, VR.IS);
        attrs.setNull(Tag.ReferringPhysicianName, VR.PN);
        
        return this;
    }

    /**
     * Build attributes for Series-level query.
     */
    public QueryAttributeBuilder forSeriesLevel(String studyInstanceUid) {
        // Query/Retrieve Level
        attrs.setString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
        
        // Match key
        attrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        
        // Return keys - Series level
        attrs.setNull(Tag.SeriesInstanceUID, VR.UI);
        attrs.setNull(Tag.SeriesNumber, VR.IS);
        attrs.setNull(Tag.SeriesDescription, VR.LO);
        attrs.setNull(Tag.Modality, VR.CS);
        attrs.setNull(Tag.BodyPartExamined, VR.CS);
        attrs.setNull(Tag.SeriesDate, VR.DA);
        attrs.setNull(Tag.SeriesTime, VR.TM);
        attrs.setNull(Tag.NumberOfSeriesRelatedInstances, VR.IS);
        
        return this;
    }

    /**
     * Build attributes for Instance-level query.
     */
    public QueryAttributeBuilder forInstanceLevel(String studyInstanceUid, 
                                                   String seriesInstanceUid) {
        // Query/Retrieve Level
        attrs.setString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        
        // Match keys
        attrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
        
        // Return keys - Instance level
        attrs.setNull(Tag.SOPInstanceUID, VR.UI);
        attrs.setNull(Tag.SOPClassUID, VR.UI);
        attrs.setNull(Tag.InstanceNumber, VR.IS);
        attrs.setNull(Tag.Rows, VR.US);
        attrs.setNull(Tag.Columns, VR.US);
        attrs.setNull(Tag.BitsAllocated, VR.US);
        attrs.setNull(Tag.ImagePositionPatient, VR.DS);
        attrs.setNull(Tag.ImageOrientationPatient, VR.DS);
        attrs.setNull(Tag.PixelSpacing, VR.DS);
        attrs.setNull(Tag.SliceThickness, VR.DS);
        
        return this;
    }

    /**
     * Add patient name filter (supports wildcards).
     */
    public QueryAttributeBuilder withPatientName(String patientName) {
        if (patientName != null && !patientName.isBlank()) {
            // Add wildcard if not already present
            String searchName = patientName.contains("*") ? patientName : patientName + "*";
            attrs.setString(Tag.PatientName, VR.PN, searchName);
        }
        return this;
    }

    /**
     * Add patient ID filter.
     */
    public QueryAttributeBuilder withPatientId(String patientId) {
        if (patientId != null && !patientId.isBlank()) {
            attrs.setString(Tag.PatientID, VR.LO, patientId);
        }
        return this;
    }

    /**
     * Add study date filter (single date).
     */
    public QueryAttributeBuilder withStudyDate(LocalDate date) {
        if (date != null) {
            attrs.setString(Tag.StudyDate, VR.DA, date.format(DICOM_DATE_FORMAT));
        }
        return this;
    }

    /**
     * Add study date range filter.
     */
    public QueryAttributeBuilder withStudyDateRange(LocalDate from, LocalDate to) {
        if (from != null || to != null) {
            String fromStr = from != null ? from.format(DICOM_DATE_FORMAT) : "";
            String toStr = to != null ? to.format(DICOM_DATE_FORMAT) : "";
            attrs.setString(Tag.StudyDate, VR.DA, fromStr + "-" + toStr);
        }
        return this;
    }

    /**
     * Add modality filter.
     */
    public QueryAttributeBuilder withModality(String modality) {
        if (modality != null && !modality.isBlank()) {
            attrs.setString(Tag.ModalitiesInStudy, VR.CS, modality);
        }
        return this;
    }

    /**
     * Add accession number filter.
     */
    public QueryAttributeBuilder withAccessionNumber(String accessionNumber) {
        if (accessionNumber != null && !accessionNumber.isBlank()) {
            attrs.setString(Tag.AccessionNumber, VR.SH, accessionNumber);
        }
        return this;
    }

    /**
     * Add study instance UID filter.
     */
    public QueryAttributeBuilder withStudyInstanceUid(String studyInstanceUid) {
        if (studyInstanceUid != null && !studyInstanceUid.isBlank()) {
            attrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        }
        return this;
    }

    /**
     * Add custom query parameters from a map.
     */
    public QueryAttributeBuilder withParameters(Map<String, String> params) {
        if (params == null) return this;
        
        params.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                switch (key.toLowerCase()) {
                    case "patientname" -> withPatientName(value);
                    case "patientid" -> withPatientId(value);
                    case "modality" -> withModality(value);
                    case "accessionnumber" -> withAccessionNumber(value);
                    case "studyinstanceuid" -> withStudyInstanceUid(value);
                }
            }
        });
        return this;
    }

    /**
     * Build and return the query attributes.
     */
    public Attributes build() {
        return attrs;
    }
}
```

---

#### 1.2 C-FIND SCU Service

**File: `dicom/network/CfindScu.java`**
```java
package com.dicomviewer.dicom.network;

import com.dicomviewer.model.Study;
import com.dicomviewer.model.Series;
import com.dicomviewer.model.Instance;
import com.dicomviewer.model.entity.ApplicationEntity;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * C-FIND Service Class User (SCU) implementation.
 * Supports Study Root and Patient Root Query/Retrieve Information Models.
 */
@Component
public class CfindScu {

    private static final Logger log = LoggerFactory.getLogger(CfindScu.class);

    private final Device device;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    // Transfer Syntaxes for C-FIND
    private static final String[] TRANSFER_SYNTAXES = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian
    };

    public CfindScu() {
        this.device = new Device("DICOMVIEWER_FIND");
        this.executor = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.device.setExecutor(executor);
        this.device.setScheduledExecutor(scheduledExecutor);
    }

    /**
     * Perform Study-level C-FIND query.
     */
    public List<Study> findStudies(ApplicationEntity localAE, 
                                    ApplicationEntity remoteAE,
                                    Attributes queryAttrs) {
        log.info("Performing C-FIND (Study level) from {} to {}",
            localAE.getAeTitle(), remoteAE.getAeTitle());

        List<Attributes> results = executeFind(
            localAE, remoteAE, 
            UID.StudyRootQueryRetrieveInformationModelFind,
            queryAttrs
        );

        return results.stream()
            .map(this::attributesToStudy)
            .toList();
    }

    /**
     * Perform Series-level C-FIND query.
     */
    public List<Series> findSeries(ApplicationEntity localAE,
                                    ApplicationEntity remoteAE,
                                    String studyInstanceUid) {
        log.info("Performing C-FIND (Series level) for study {} from {} to {}",
            studyInstanceUid, localAE.getAeTitle(), remoteAE.getAeTitle());

        Attributes queryAttrs = QueryAttributeBuilder.create()
            .forSeriesLevel(studyInstanceUid)
            .build();

        List<Attributes> results = executeFind(
            localAE, remoteAE,
            UID.StudyRootQueryRetrieveInformationModelFind,
            queryAttrs
        );

        return results.stream()
            .map(this::attributesToSeries)
            .toList();
    }

    /**
     * Perform Instance-level C-FIND query.
     */
    public List<Instance> findInstances(ApplicationEntity localAE,
                                         ApplicationEntity remoteAE,
                                         String studyInstanceUid,
                                         String seriesInstanceUid) {
        log.info("Performing C-FIND (Instance level) for series {} from {} to {}",
            seriesInstanceUid, localAE.getAeTitle(), remoteAE.getAeTitle());

        Attributes queryAttrs = QueryAttributeBuilder.create()
            .forInstanceLevel(studyInstanceUid, seriesInstanceUid)
            .build();

        List<Attributes> results = executeFind(
            localAE, remoteAE,
            UID.StudyRootQueryRetrieveInformationModelFind,
            queryAttrs
        );

        return results.stream()
            .map(this::attributesToInstance)
            .toList();
    }

    /**
     * Execute C-FIND operation and collect results.
     */
    private List<Attributes> executeFind(ApplicationEntity localAE,
                                          ApplicationEntity remoteAE,
                                          String sopClassUid,
                                          Attributes queryAttrs) {
        List<Attributes> results = new ArrayList<>();
        
        ApplicationEntity ae = createApplicationEntity(localAE);
        Connection localConn = createConnection(localAE);
        Connection remoteConn = createRemoteConnection(remoteAE);

        ae.addConnection(localConn);
        device.addApplicationEntity(ae);
        device.addConnection(localConn);

        Association association = null;
        try {
            AAssociateRQ rq = createFindAssociateRequest(localAE, remoteAE, sopClassUid);
            association = ae.connect(remoteConn, rq);

            DimseRSP response = association.cfind(
                sopClassUid,
                Priority.NORMAL,
                queryAttrs,
                UID.ImplicitVRLittleEndian,
                0  // no limit
            );

            while (response.next()) {
                Attributes cmd = response.getCommand();
                int status = cmd.getInt(Tag.Status, -1);

                if (Status.isPending(status)) {
                    Attributes data = response.getDataset();
                    if (data != null) {
                        results.add(new Attributes(data));
                    }
                } else if (status != Status.Success) {
                    log.warn("C-FIND returned status: 0x{}", Integer.toHexString(status));
                }
            }

            log.info("C-FIND completed with {} results", results.size());
            return results;

        } catch (IOException | InterruptedException | IncompatibleConnectionException |
                 GeneralSecurityException e) {
            log.error("C-FIND to {} failed: {}", remoteAE.getAeTitle(), e.getMessage());
            throw new RuntimeException("C-FIND operation failed", e);
        } finally {
            if (association != null && association.isReadyForDataTransfer()) {
                try {
                    association.release();
                } catch (IOException e) {
                    log.warn("Error releasing association: {}", e.getMessage());
                }
            }
            device.removeApplicationEntity(ae.getAETitle());
            device.removeConnection(localConn);
        }
    }

    private ApplicationEntity createApplicationEntity(ApplicationEntity config) {
        ApplicationEntity ae = new ApplicationEntity(config.getAeTitle());
        ae.setAssociationAcceptor(false);
        ae.setAssociationInitiator(true);
        return ae;
    }

    private Connection createConnection(ApplicationEntity config) {
        Connection conn = new Connection();
        conn.setConnectTimeout(config.getConnectionTimeout());
        conn.setRequestTimeout(config.getResponseTimeout());
        return conn;
    }

    private Connection createRemoteConnection(ApplicationEntity remoteAE) {
        Connection conn = new Connection();
        conn.setHostname(remoteAE.getHostname());
        conn.setPort(remoteAE.getPort());
        return conn;
    }

    private AAssociateRQ createFindAssociateRequest(ApplicationEntity localAE,
                                                     ApplicationEntity remoteAE,
                                                     String sopClassUid) {
        AAssociateRQ rq = new AAssociateRQ();
        rq.setCallingAET(localAE.getAeTitle());
        rq.setCalledAET(remoteAE.getAeTitle());
        
        // Add presentation context for C-FIND
        rq.addPresentationContext(new PresentationContext(
            1, sopClassUid, TRANSFER_SYNTAXES
        ));
        
        return rq;
    }

    // Conversion methods
    private Study attributesToStudy(Attributes attrs) {
        Study study = new Study();
        study.setStudyInstanceUid(attrs.getString(Tag.StudyInstanceUID));
        study.setPatientId(attrs.getString(Tag.PatientID));
        study.setPatientName(attrs.getString(Tag.PatientName));
        study.setPatientBirthDate(attrs.getString(Tag.PatientBirthDate));
        study.setPatientSex(attrs.getString(Tag.PatientSex));
        study.setStudyDate(attrs.getString(Tag.StudyDate));
        study.setStudyTime(attrs.getString(Tag.StudyTime));
        study.setStudyDescription(attrs.getString(Tag.StudyDescription));
        study.setAccessionNumber(attrs.getString(Tag.AccessionNumber));
        String[] modalities = attrs.getStrings(Tag.ModalitiesInStudy);
        if (modalities != null && modalities.length > 0) {
            study.setModalitiesInStudy(Arrays.asList(modalities));
        } else {
            study.setModalitiesInStudy(List.of());
        }
        study.setNumberOfStudyRelatedSeries(attrs.getInt(Tag.NumberOfStudyRelatedSeries, 0));
        study.setNumberOfStudyRelatedInstances(attrs.getInt(Tag.NumberOfStudyRelatedInstances, 0));
        return study;
    }

    private Series attributesToSeries(Attributes attrs) {
        Series series = new Series();
        series.setSeriesInstanceUid(attrs.getString(Tag.SeriesInstanceUID));
        series.setSeriesNumber(attrs.getInt(Tag.SeriesNumber, 0));
        series.setSeriesDescription(attrs.getString(Tag.SeriesDescription));
        series.setModality(attrs.getString(Tag.Modality));
        series.setBodyPartExamined(attrs.getString(Tag.BodyPartExamined));
        series.setNumberOfSeriesRelatedInstances(attrs.getInt(Tag.NumberOfSeriesRelatedInstances, 0));
        return series;
    }

    private Instance attributesToInstance(Attributes attrs) {
        Instance instance = new Instance();
        instance.setSopInstanceUid(attrs.getString(Tag.SOPInstanceUID));
        instance.setSopClassUid(attrs.getString(Tag.SOPClassUID));
        instance.setInstanceNumber(attrs.getInt(Tag.InstanceNumber, 0));
        instance.setRows(attrs.getInt(Tag.Rows, 0));
        instance.setColumns(attrs.getInt(Tag.Columns, 0));
        return instance;
    }

    /**
     * Cleanup resources on shutdown.
     */
    public void shutdown() {
        executor.shutdown();
        scheduledExecutor.shutdown();
    }
}
```

---

### 2. Unified Query Service

#### 2.1 Query Service Interface

**File: `service/UnifiedQueryService.java`**
```java
package com.dicomviewer.service;

import com.dicomviewer.model.Study;
import com.dicomviewer.model.Series;
import com.dicomviewer.model.Instance;
import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import com.dicomviewer.dicom.network.CfindScu;
import com.dicomviewer.dicom.network.QueryAttributeBuilder;
import com.dicomviewer.dicom.web.QidoRsClient;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Unified query service that abstracts query operations across 
 * DICOMweb and traditional DICOM protocols.
 */
@Service
public class UnifiedQueryService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedQueryService.class);

    private final ApplicationEntityService aeService;
    private final CfindScu cfindScu;
    private final QidoRsClient qidoRsClient;

    public UnifiedQueryService(ApplicationEntityService aeService,
                               CfindScu cfindScu,
                               QidoRsClient qidoRsClient) {
        this.aeService = aeService;
        this.cfindScu = cfindScu;
        this.qidoRsClient = qidoRsClient;
    }

    /**
     * Search for studies across all enabled PACS sources.
     */
    public List<Study> searchStudies(Map<String, String> queryParams) {
        List<ApplicationEntity> sources = aeService.getEnabledRemoteAEs();
        ApplicationEntity localAE = aeService.getLocalAE();

        List<CompletableFuture<List<Study>>> futures = sources.stream()
            .map(source -> CompletableFuture.supplyAsync(() -> 
                queryStudiesFromSource(localAE, source, queryParams)))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Search for studies on a specific PACS source.
     */
    public List<Study> searchStudies(Long sourceId, Map<String, String> queryParams) {
        ApplicationEntity source = aeService.getAEById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        ApplicationEntity localAE = aeService.getLocalAE();

        return queryStudiesFromSource(localAE, source, queryParams);
    }

    /**
     * Get series for a study from a specific PACS source.
     */
    @Cacheable(value = "series", key = "#sourceId + '-' + #studyInstanceUid")
    public List<Series> getSeriesForStudy(Long sourceId, String studyInstanceUid) {
        ApplicationEntity source = aeService.getAEById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        ApplicationEntity localAE = aeService.getLocalAE();

        if (source.getAeType() == AEType.REMOTE_DICOMWEB) {
            return qidoRsClient.getSeriesForStudy(source.getDicomWebUrl(), studyInstanceUid);
        } else {
            return cfindScu.findSeries(localAE, source, studyInstanceUid);
        }
    }

    /**
     * Get instances for a series from a specific PACS source.
     */
    @Cacheable(value = "instances", key = "#sourceId + '-' + #seriesInstanceUid")
    public List<Instance> getInstancesForSeries(Long sourceId,
                                                 String studyInstanceUid,
                                                 String seriesInstanceUid) {
        ApplicationEntity source = aeService.getAEById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        ApplicationEntity localAE = aeService.getLocalAE();

        if (source.getAeType() == AEType.REMOTE_DICOMWEB) {
            return qidoRsClient.getInstancesForSeries(
                source.getDicomWebUrl(), studyInstanceUid, seriesInstanceUid);
        } else {
            return cfindScu.findInstances(localAE, source, studyInstanceUid, seriesInstanceUid);
        }
    }

    /**
     * Query studies from a single source (DICOMweb or Legacy DICOM).
     */
    private List<Study> queryStudiesFromSource(ApplicationEntity localAE,
                                                ApplicationEntity source,
                                                Map<String, String> queryParams) {
        try {
            if (source.getAeType() == AEType.REMOTE_DICOMWEB) {
                log.debug("Querying DICOMweb source: {}", source.getAeTitle());
                List<Study> studies = qidoRsClient.searchStudies(
                    source.getDicomWebUrl(), queryParams);
                studies.forEach(s -> s.setSourcePacs(createPacsSource(source)));
                return studies;
            } else {
                log.debug("Querying legacy PACS: {}", source.getAeTitle());
                Attributes queryAttrs = QueryAttributeBuilder.create()
                    .forStudyLevel()
                    .withParameters(queryParams)
                    .build();
                List<Study> studies = cfindScu.findStudies(localAE, source, queryAttrs);
                studies.forEach(s -> s.setSourcePacs(createPacsSource(source)));
                return studies;
            }
        } catch (Exception e) {
            log.error("Failed to query source {}: {}", source.getAeTitle(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private com.dicomviewer.model.PacsSource createPacsSource(ApplicationEntity ae) {
        com.dicomviewer.model.PacsSource source = new com.dicomviewer.model.PacsSource();
        source.setId(ae.getId().toString());
        source.setName(ae.getAeTitle());
        source.setType(ae.getAeType() == AEType.REMOTE_DICOMWEB ? "DICOMWEB" : "LEGACY");
        return source;
    }
}
```

---

## Week 9-10: Retrieval Operations (C-MOVE/C-STORE)

### 1. C-MOVE SCU Implementation

#### 1.1 C-MOVE SCU Service

**Tasks:**
- [ ] Implement C-MOVE Service Class User for initiating retrieval requests
- [ ] Handle C-MOVE sub-operations and progress tracking
- [ ] Configure destination AE for image delivery
- [ ] Implement retrieval status monitoring

**File: `dicom/network/CmoveScu.java`**
```java
package com.dicomviewer.dicom.network;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.RetrievalProgress;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * C-MOVE Service Class User (SCU) implementation.
 * Initiates retrieval of DICOM objects from remote PACS.
 */
@Component
public class CmoveScu {

    private static final Logger log = LoggerFactory.getLogger(CmoveScu.class);

    private final Device device;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;
    private final ConcurrentMap<String, RetrievalProgress> activeRetrievals;

    // Transfer Syntaxes for C-MOVE
    private static final String[] TRANSFER_SYNTAXES = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian
    };

    public CmoveScu() {
        this.device = new Device("DICOMVIEWER_MOVE");
        this.executor = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.device.setExecutor(executor);
        this.device.setScheduledExecutor(scheduledExecutor);
        this.activeRetrievals = new ConcurrentHashMap<>();
    }

    /**
     * Initiate C-MOVE to retrieve a study.
     *
     * @param localAE          Local AE configuration
     * @param remoteAE         Remote PACS to retrieve from
     * @param destinationAE    Destination AE (usually local) to receive images
     * @param studyInstanceUid Study to retrieve
     * @param progressCallback Optional callback for progress updates
     * @return Retrieval ID for tracking progress
     */
    public String moveStudy(ApplicationEntity localAE,
                            ApplicationEntity remoteAE,
                            ApplicationEntity destinationAE,
                            String studyInstanceUid,
                            Consumer<RetrievalProgress> progressCallback) {
        String retrievalId = UUID.randomUUID().toString();
        
        Attributes queryAttrs = new Attributes();
        queryAttrs.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        queryAttrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);

        return executeMove(retrievalId, localAE, remoteAE, destinationAE,
            UID.StudyRootQueryRetrieveInformationModelMove, queryAttrs, progressCallback);
    }

    /**
     * Initiate C-MOVE to retrieve a series.
     */
    public String moveSeries(ApplicationEntity localAE,
                             ApplicationEntity remoteAE,
                             ApplicationEntity destinationAE,
                             String studyInstanceUid,
                             String seriesInstanceUid,
                             Consumer<RetrievalProgress> progressCallback) {
        String retrievalId = UUID.randomUUID().toString();
        
        Attributes queryAttrs = new Attributes();
        queryAttrs.setString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
        queryAttrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        queryAttrs.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);

        return executeMove(retrievalId, localAE, remoteAE, destinationAE,
            UID.StudyRootQueryRetrieveInformationModelMove, queryAttrs, progressCallback);
    }

    /**
     * Initiate C-MOVE to retrieve specific instances.
     */
    public String moveInstances(ApplicationEntity localAE,
                                ApplicationEntity remoteAE,
                                ApplicationEntity destinationAE,
                                String studyInstanceUid,
                                String seriesInstanceUid,
                                String[] sopInstanceUids,
                                Consumer<RetrievalProgress> progressCallback) {
        String retrievalId = UUID.randomUUID().toString();
        
        Attributes queryAttrs = new Attributes();
        queryAttrs.setString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        queryAttrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        queryAttrs.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
        queryAttrs.setString(Tag.SOPInstanceUID, VR.UI, sopInstanceUids);

        return executeMove(retrievalId, localAE, remoteAE, destinationAE,
            UID.StudyRootQueryRetrieveInformationModelMove, queryAttrs, progressCallback);
    }

    /**
     * Execute C-MOVE operation asynchronously.
     */
    private String executeMove(String retrievalId,
                               ApplicationEntity localAE,
                               ApplicationEntity remoteAE,
                               ApplicationEntity destinationAE,
                               String sopClassUid,
                               Attributes queryAttrs,
                               Consumer<RetrievalProgress> progressCallback) {
        
        RetrievalProgress progress = new RetrievalProgress(retrievalId);
        progress.setStatus(RetrievalProgress.Status.PENDING);
        activeRetrievals.put(retrievalId, progress);

        CompletableFuture.runAsync(() -> {
            log.info("Starting C-MOVE retrieval {} from {} to {}",
                retrievalId, remoteAE.getAeTitle(), destinationAE.getAeTitle());

            progress.setStatus(RetrievalProgress.Status.IN_PROGRESS);
            notifyProgress(progress, progressCallback);

            org.dcm4che3.net.ApplicationEntity ae = createApplicationEntity(localAE);
            Connection localConn = createConnection(localAE);
            Connection remoteConn = createRemoteConnection(remoteAE);

            ae.addConnection(localConn);
            device.addApplicationEntity(ae);
            device.addConnection(localConn);

            Association association = null;
            try {
                AAssociateRQ rq = createMoveAssociateRequest(localAE, remoteAE, sopClassUid);
                association = ae.connect(remoteConn, rq);

                DimseRSP response = association.cmove(
                    sopClassUid,
                    Priority.NORMAL,
                    queryAttrs,
                    UID.ImplicitVRLittleEndian,
                    destinationAE.getAeTitle()
                );

                while (response.next()) {
                    Attributes cmd = response.getCommand();
                    int status = cmd.getInt(Tag.Status, -1);

                    if (Status.isPending(status)) {
                        // Update progress from sub-operation counts
                        int remaining = cmd.getInt(Tag.NumberOfRemainingSuboperations, 0);
                        int completed = cmd.getInt(Tag.NumberOfCompletedSuboperations, 0);
                        int failed = cmd.getInt(Tag.NumberOfFailedSuboperations, 0);
                        int warning = cmd.getInt(Tag.NumberOfWarningSuboperations, 0);

                        progress.setTotal(remaining + completed + failed + warning);
                        progress.setCompleted(completed);
                        progress.setFailed(failed);
                        progress.setWarnings(warning);
                        
                        notifyProgress(progress, progressCallback);
                    } else if (status == Status.Success) {
                        progress.setStatus(RetrievalProgress.Status.COMPLETED);
                        log.info("C-MOVE {} completed successfully", retrievalId);
                    } else if (Status.isSubOperationsFailure(status)) {
                        progress.setStatus(RetrievalProgress.Status.COMPLETED_WITH_ERRORS);
                        log.warn("C-MOVE {} completed with failures", retrievalId);
                    } else {
                        progress.setStatus(RetrievalProgress.Status.FAILED);
                        progress.setErrorMessage("Move failed with status: 0x" + 
                            Integer.toHexString(status));
                        log.error("C-MOVE {} failed with status: 0x{}", 
                            retrievalId, Integer.toHexString(status));
                    }
                }

            } catch (Exception e) {
                log.error("C-MOVE {} failed: {}", retrievalId, e.getMessage());
                progress.setStatus(RetrievalProgress.Status.FAILED);
                progress.setErrorMessage(e.getMessage());
            } finally {
                if (association != null && association.isReadyForDataTransfer()) {
                    try {
                        association.release();
                    } catch (IOException e) {
                        log.warn("Error releasing association: {}", e.getMessage());
                    }
                }
                device.removeApplicationEntity(ae.getAETitle());
                device.removeConnection(localConn);
                notifyProgress(progress, progressCallback);
            }
        }, executor);

        return retrievalId;
    }

    /**
     * Get current retrieval progress.
     */
    public RetrievalProgress getProgress(String retrievalId) {
        return activeRetrievals.get(retrievalId);
    }

    /**
     * Cancel an active retrieval.
     */
    public boolean cancelRetrieval(String retrievalId) {
        RetrievalProgress progress = activeRetrievals.get(retrievalId);
        if (progress != null && progress.getStatus() == RetrievalProgress.Status.IN_PROGRESS) {
            progress.setStatus(RetrievalProgress.Status.CANCELLED);
            return true;
        }
        return false;
    }

    private void notifyProgress(RetrievalProgress progress, 
                                Consumer<RetrievalProgress> callback) {
        if (callback != null) {
            callback.accept(progress);
        }
    }

    private org.dcm4che3.net.ApplicationEntity createApplicationEntity(
            ApplicationEntity config) {
        org.dcm4che3.net.ApplicationEntity ae = 
            new org.dcm4che3.net.ApplicationEntity(config.getAeTitle());
        ae.setAssociationAcceptor(false);
        ae.setAssociationInitiator(true);
        return ae;
    }

    private Connection createConnection(ApplicationEntity config) {
        Connection conn = new Connection();
        conn.setConnectTimeout(config.getConnectionTimeout());
        conn.setRequestTimeout(config.getResponseTimeout());
        return conn;
    }

    private Connection createRemoteConnection(ApplicationEntity remoteAE) {
        Connection conn = new Connection();
        conn.setHostname(remoteAE.getHostname());
        conn.setPort(remoteAE.getPort());
        return conn;
    }

    private AAssociateRQ createMoveAssociateRequest(ApplicationEntity localAE,
                                                     ApplicationEntity remoteAE,
                                                     String sopClassUid) {
        AAssociateRQ rq = new AAssociateRQ();
        rq.setCallingAET(localAE.getAeTitle());
        rq.setCalledAET(remoteAE.getAeTitle());
        rq.addPresentationContext(new PresentationContext(
            1, sopClassUid, TRANSFER_SYNTAXES
        ));
        return rq;
    }

    /**
     * Cleanup resources on shutdown.
     */
    public void shutdown() {
        executor.shutdown();
        scheduledExecutor.shutdown();
    }
}
```

---

#### 1.2 Retrieval Progress Model

**File: `model/RetrievalProgress.java`**
```java
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
```

---

### 2. C-STORE SCP Implementation

#### 2.1 C-STORE SCP Service

**Tasks:**
- [ ] Implement C-STORE Service Class Provider for receiving DICOM instances
- [ ] Handle incoming DICOM files and extract metadata
- [ ] Store received instances to local storage
- [ ] Implement association negotiation with multiple transfer syntaxes

**File: `dicom/network/CstoreScp.java`**
```java
package com.dicomviewer.dicom.network;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.service.StorageService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * C-STORE Service Class Provider (SCP) implementation.
 * Receives DICOM instances from C-MOVE operations.
 */
@Component
public class CstoreScp {

    private static final Logger log = LoggerFactory.getLogger(CstoreScp.class);

    private final StorageService storageService;
    private final DicomNetworkConfig networkConfig;
    
    private Device device;
    private org.dcm4che3.net.ApplicationEntity ae;
    private Connection conn;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutor;
    private boolean running = false;

    // Supported Storage SOP Classes
    private static final String[] STORAGE_SOP_CLASSES = {
        UID.CTImageStorage,
        UID.MRImageStorage,
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.UltrasoundImageStorage,
        UID.SecondaryCaptureImageStorage,
        UID.XRayAngiographicImageStorage,
        UID.EnhancedCTImageStorage,
        UID.EnhancedMRImageStorage,
        UID.PositronEmissionTomographyImageStorage,
        UID.NuclearMedicineImageStorage
    };

    // Supported Transfer Syntaxes
    private static final String[] TRANSFER_SYNTAXES = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.JPEGLossless,
        UID.JPEGLosslessSV1,
        UID.JPEG2000Lossless,
        UID.JPEGBaseline8Bit,
        UID.JPEGExtended12Bit
    };

    public CstoreScp(StorageService storageService, DicomNetworkConfig networkConfig) {
        this.storageService = storageService;
        this.networkConfig = networkConfig;
    }

    @PostConstruct
    public void init() {
        if (networkConfig.isAutoStart()) {
            start();
        }
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    /**
     * Start the C-STORE SCP service.
     */
    public synchronized void start() {
        if (running) {
            log.warn("C-STORE SCP is already running");
            return;
        }

        try {
            ApplicationEntity localAE = networkConfig.getLocalAE();
            
            this.executor = Executors.newCachedThreadPool();
            this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

            // Create device
            this.device = new Device("DICOMVIEWER_STORE");
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);

            // Create connection
            this.conn = new Connection();
            conn.setPort(localAE.getPort());
            conn.setHostname("0.0.0.0");
            conn.setAcceptTimeout(30000);
            conn.setConnectTimeout(30000);

            // Create Application Entity
            this.ae = new org.dcm4che3.net.ApplicationEntity(localAE.getAeTitle());
            ae.setAssociationAcceptor(true);
            ae.setAssociationInitiator(false);
            ae.addConnection(conn);

            // Register C-STORE SCP handler
            DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
            serviceRegistry.addDicomService(new CStoreSCPHandler());
            ae.setDimseRQHandler(serviceRegistry);

            // Add supported transfer syntaxes for each SOP class
            for (String sopClass : STORAGE_SOP_CLASSES) {
                ae.addTransferCapability(new TransferCapability(
                    null, sopClass, TransferCapability.Role.SCP, TRANSFER_SYNTAXES
                ));
            }

            device.addApplicationEntity(ae);
            device.addConnection(conn);

            // Start listening
            device.bindConnections();
            running = true;

            log.info("C-STORE SCP started on port {} with AE title {}",
                localAE.getPort(), localAE.getAeTitle());

        } catch (Exception e) {
            log.error("Failed to start C-STORE SCP: {}", e.getMessage());
            throw new RuntimeException("Failed to start C-STORE SCP", e);
        }
    }

    /**
     * Stop the C-STORE SCP service.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }

        try {
            device.unbindConnections();
            executor.shutdown();
            scheduledExecutor.shutdown();
            running = false;
            log.info("C-STORE SCP stopped");
        } catch (Exception e) {
            log.error("Error stopping C-STORE SCP: {}", e.getMessage());
        }
    }

    /**
     * Check if the SCP is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Inner class implementing the C-STORE SCP handler.
     */
    private class CStoreSCPHandler extends BasicCStoreSCP {

        @Override
        protected void store(Association as, PresentationContext pc, 
                           Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            
            String sopClassUID = rq.getString(Tag.AffectedSOPClassUID);
            String sopInstanceUID = rq.getString(Tag.AffectedSOPInstanceUID);
            String transferSyntax = pc.getTransferSyntax();

            log.debug("Receiving C-STORE: SOP Instance UID = {}", sopInstanceUID);

            try {
                // Create temporary file to store the data
                Path tempFile = Files.createTempFile("dcm_", ".tmp");
                
                // Read DICOM data
                Attributes dataset = data.readDataset(transferSyntax);
                Attributes fmi = as.createFileMetaInformation(sopInstanceUID, 
                    sopClassUID, transferSyntax);

                // Write to temporary file
                try (DicomOutputStream dos = new DicomOutputStream(tempFile.toFile())) {
                    dos.writeDataset(fmi, dataset);
                }

                // Process the received instance
                storageService.storeInstance(tempFile.toFile(), dataset, fmi);

                // Clean up temp file
                Files.deleteIfExists(tempFile);

                log.info("Successfully stored instance: {}", sopInstanceUID);

            } catch (Exception e) {
                log.error("Failed to store instance {}: {}", sopInstanceUID, e.getMessage());
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }
        }
    }
}
```

---

### 3. Local Storage Management

#### 3.1 Storage Service

**File: `service/StorageService.java`**
```java
package com.dicomviewer.service;

import com.dicomviewer.model.entity.StoredInstance;
import com.dicomviewer.repository.StoredInstanceRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/**
 * Service for managing local DICOM storage.
 */
@Service
@Transactional
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Value("${dicom.storage.path:/var/dicom/storage}")
    private String storagePath;

    @Value("${dicom.storage.structure:hierarchical}")
    private String storageStructure;

    private final StoredInstanceRepository instanceRepository;

    public StorageService(StoredInstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    /**
     * Store a received DICOM instance.
     */
    public StoredInstance storeInstance(File sourceFile, Attributes dataset, 
                                        Attributes fileMetaInfo) throws IOException {
        String studyUID = dataset.getString(Tag.StudyInstanceUID);
        String seriesUID = dataset.getString(Tag.SeriesInstanceUID);
        String sopInstanceUID = dataset.getString(Tag.SOPInstanceUID);

        // Check if already stored
        if (instanceRepository.existsBySopInstanceUid(sopInstanceUID)) {
            log.debug("Instance already stored: {}", sopInstanceUID);
            return instanceRepository.findBySopInstanceUid(sopInstanceUID).orElse(null);
        }

        // Calculate storage path
        Path targetPath = calculateStoragePath(studyUID, seriesUID, sopInstanceUID);
        Files.createDirectories(targetPath.getParent());

        // Move file to storage location
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Create database entry
        StoredInstance instance = new StoredInstance();
        instance.setSopInstanceUid(sopInstanceUID);
        instance.setSopClassUid(dataset.getString(Tag.SOPClassUID));
        instance.setStudyInstanceUid(studyUID);
        instance.setSeriesInstanceUid(seriesUID);
        instance.setFilePath(targetPath.toString());
        instance.setFileSize(Files.size(targetPath));
        instance.setTransferSyntaxUid(fileMetaInfo.getString(Tag.TransferSyntaxUID));
        
        // Extract additional metadata
        instance.setPatientId(dataset.getString(Tag.PatientID));
        instance.setPatientName(dataset.getString(Tag.PatientName));
        instance.setStudyDate(dataset.getString(Tag.StudyDate));
        instance.setModality(dataset.getString(Tag.Modality));
        instance.setInstanceNumber(dataset.getInt(Tag.InstanceNumber, 0));
        instance.setRows(dataset.getInt(Tag.Rows, 0));
        instance.setColumns(dataset.getInt(Tag.Columns, 0));
        instance.setStoredAt(Instant.now());

        StoredInstance saved = instanceRepository.save(instance);
        log.info("Stored instance: {} at {}", sopInstanceUID, targetPath);

        return saved;
    }

    /**
     * Get the file for a stored instance.
     */
    public File getInstanceFile(String sopInstanceUid) {
        StoredInstance instance = instanceRepository.findBySopInstanceUid(sopInstanceUid)
            .orElseThrow(() -> new IllegalArgumentException(
                "Instance not found: " + sopInstanceUid));
        return new File(instance.getFilePath());
    }

    /**
     * Check if an instance exists in local storage.
     */
    public boolean hasInstance(String sopInstanceUid) {
        return instanceRepository.existsBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Delete a stored instance.
     */
    public void deleteInstance(String sopInstanceUid) throws IOException {
        StoredInstance instance = instanceRepository.findBySopInstanceUid(sopInstanceUid)
            .orElseThrow(() -> new IllegalArgumentException(
                "Instance not found: " + sopInstanceUid));
        
        Files.deleteIfExists(Paths.get(instance.getFilePath()));
        instanceRepository.delete(instance);
        
        log.info("Deleted instance: {}", sopInstanceUid);
    }

    /**
     * Delete all instances for a study.
     */
    public void deleteStudy(String studyInstanceUid) throws IOException {
        var instances = instanceRepository.findByStudyInstanceUid(studyInstanceUid);
        for (StoredInstance instance : instances) {
            Files.deleteIfExists(Paths.get(instance.getFilePath()));
        }
        instanceRepository.deleteAll(instances);
        
        // Try to remove empty directories
        Path studyDir = Paths.get(storagePath, studyInstanceUid);
        if (Files.exists(studyDir)) {
            Files.walk(studyDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ignored) {}
                });
        }
        
        log.info("Deleted study: {}", studyInstanceUid);
    }

    /**
     * Get storage statistics.
     */
    public StorageStats getStorageStats() {
        StorageStats stats = new StorageStats();
        stats.setTotalInstances(instanceRepository.count());
        stats.setTotalSize(instanceRepository.sumFileSize());
        stats.setStudyCount(instanceRepository.countDistinctStudies());
        stats.setSeriesCount(instanceRepository.countDistinctSeries());
        return stats;
    }

    /**
     * Calculate storage path based on configuration.
     */
    private Path calculateStoragePath(String studyUID, String seriesUID, 
                                      String sopInstanceUID) {
        if ("flat".equals(storageStructure)) {
            return Paths.get(storagePath, sopInstanceUID + ".dcm");
        } else {
            // Hierarchical: storage/studyUID/seriesUID/sopInstanceUID.dcm
            return Paths.get(storagePath, studyUID, seriesUID, sopInstanceUID + ".dcm");
        }
    }

    /**
     * Storage statistics DTO.
     */
    public static class StorageStats {
        private long totalInstances;
        private long totalSize;
        private long studyCount;
        private long seriesCount;

        // Getters and setters
        public long getTotalInstances() { return totalInstances; }
        public void setTotalInstances(long totalInstances) { this.totalInstances = totalInstances; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public long getStudyCount() { return studyCount; }
        public void setStudyCount(long studyCount) { this.studyCount = studyCount; }
        
        public long getSeriesCount() { return seriesCount; }
        public void setSeriesCount(long seriesCount) { this.seriesCount = seriesCount; }
        
        public String getFormattedSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return (totalSize / 1024) + " KB";
            if (totalSize < 1024 * 1024 * 1024) return (totalSize / (1024 * 1024)) + " MB";
            return (totalSize / (1024 * 1024 * 1024)) + " GB";
        }
    }
}
```

---

#### 3.2 Stored Instance Entity

**File: `model/entity/StoredInstance.java`**
```java
package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a locally stored DICOM instance.
 */
@Entity
@Table(name = "stored_instances", indexes = {
    @Index(name = "idx_sop_instance_uid", columnList = "sop_instance_uid", unique = true),
    @Index(name = "idx_study_instance_uid", columnList = "study_instance_uid"),
    @Index(name = "idx_series_instance_uid", columnList = "series_instance_uid"),
    @Index(name = "idx_patient_id", columnList = "patient_id")
})
public class StoredInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sop_instance_uid", nullable = false, unique = true)
    private String sopInstanceUid;

    @Column(name = "sop_class_uid")
    private String sopClassUid;

    @Column(name = "study_instance_uid", nullable = false)
    private String studyInstanceUid;

    @Column(name = "series_instance_uid", nullable = false)
    private String seriesInstanceUid;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "transfer_syntax_uid")
    private String transferSyntaxUid;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "study_date")
    private String studyDate;

    @Column(name = "modality")
    private String modality;

    @Column(name = "instance_number")
    private Integer instanceNumber;

    @Column(name = "rows")
    private Integer rows;

    @Column(name = "columns")
    private Integer columns;

    @Column(name = "stored_at", nullable = false)
    private Instant storedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSopInstanceUid() { return sopInstanceUid; }
    public void setSopInstanceUid(String sopInstanceUid) { this.sopInstanceUid = sopInstanceUid; }

    public String getSopClassUid() { return sopClassUid; }
    public void setSopClassUid(String sopClassUid) { this.sopClassUid = sopClassUid; }

    public String getStudyInstanceUid() { return studyInstanceUid; }
    public void setStudyInstanceUid(String studyInstanceUid) { 
        this.studyInstanceUid = studyInstanceUid; 
    }

    public String getSeriesInstanceUid() { return seriesInstanceUid; }
    public void setSeriesInstanceUid(String seriesInstanceUid) { 
        this.seriesInstanceUid = seriesInstanceUid; 
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getTransferSyntaxUid() { return transferSyntaxUid; }
    public void setTransferSyntaxUid(String transferSyntaxUid) { 
        this.transferSyntaxUid = transferSyntaxUid; 
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getStudyDate() { return studyDate; }
    public void setStudyDate(String studyDate) { this.studyDate = studyDate; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public Integer getInstanceNumber() { return instanceNumber; }
    public void setInstanceNumber(Integer instanceNumber) { this.instanceNumber = instanceNumber; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }

    public Instant getStoredAt() { return storedAt; }
    public void setStoredAt(Instant storedAt) { this.storedAt = storedAt; }

    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}
```

---

#### 3.3 Stored Instance Repository

**File: `repository/StoredInstanceRepository.java`**
```java
package com.dicomviewer.repository;

import com.dicomviewer.model.entity.StoredInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoredInstanceRepository extends JpaRepository<StoredInstance, Long> {

    Optional<StoredInstance> findBySopInstanceUid(String sopInstanceUid);

    boolean existsBySopInstanceUid(String sopInstanceUid);

    List<StoredInstance> findByStudyInstanceUid(String studyInstanceUid);

    List<StoredInstance> findBySeriesInstanceUid(String seriesInstanceUid);

    List<StoredInstance> findByPatientId(String patientId);

    @Query("SELECT COALESCE(SUM(s.fileSize), 0) FROM StoredInstance s")
    long sumFileSize();

    @Query("SELECT COUNT(DISTINCT s.studyInstanceUid) FROM StoredInstance s")
    long countDistinctStudies();

    @Query("SELECT COUNT(DISTINCT s.seriesInstanceUid) FROM StoredInstance s")
    long countDistinctSeries();

    void deleteByStudyInstanceUid(String studyInstanceUid);

    void deleteBySeriesInstanceUid(String seriesInstanceUid);
}
```

---

## Acceptance Criteria

### Phase 2 Completion Checklist

#### DICOM Network Foundation
- [ ] Application Entity model created with all required fields
- [ ] AE CRUD operations implemented (Create, Read, Update, Delete)
- [ ] AE configuration persisted to database
- [ ] Local AE properly configured on application startup
- [ ] REST API for AE management functional

#### C-ECHO Implementation
- [ ] C-ECHO SCU successfully verifies connectivity to remote PACS
- [ ] C-ECHO response time under 500ms for local network
- [ ] Connection timeout properly handled
- [ ] Echo status persisted and displayed in UI
- [ ] Batch echo test for all configured PACS

#### C-FIND Implementation
- [ ] Study-level C-FIND returns correct results
- [ ] Series-level C-FIND returns correct results
- [ ] Instance-level C-FIND returns correct results
- [ ] Wildcard search works for patient name (e.g., "DOE*")
- [ ] Date range queries work correctly
- [ ] Query pagination functional (offset/limit)
- [ ] Results cached appropriately
- [ ] Query timeout handled gracefully

#### C-MOVE/C-STORE Implementation
- [ ] C-MOVE successfully initiates retrieval from remote PACS
- [ ] C-STORE SCP receives and stores DICOM instances
- [ ] Progress tracking provides accurate completion percentage
- [ ] Multiple transfer syntaxes supported
- [ ] Failed retrievals properly reported
- [ ] Local storage organized hierarchically
- [ ] Metadata extracted and stored in database

#### Unified Query Interface
- [ ] Queries work across both DICOMweb and legacy PACS
- [ ] Results include source PACS information
- [ ] Error from one source doesn't affect others
- [ ] Parallel query execution for multiple sources

#### Frontend Requirements
- [ ] PACS configuration UI functional
- [ ] Add/Edit/Delete PACS configurations
- [ ] Connection test button works
- [ ] Status indicators updated in real-time
- [ ] Error messages displayed appropriately
- [ ] Retrieval progress shown to user

#### Performance Requirements
| Metric | Target | Measurement |
|--------|--------|-------------|
| C-ECHO response | < 500ms | Network latency test |
| C-FIND (100 results) | < 3s | Query timing |
| C-MOVE initiation | < 1s | Request timing |
| C-STORE processing | < 100ms/instance | Average processing time |
| Connection pool efficiency | > 90% | Connection reuse ratio |

---

## Testing Strategy

### Backend Unit Tests

#### AE Service Tests
```java
@SpringBootTest
class ApplicationEntityServiceTest {

    @Autowired
    private ApplicationEntityService aeService;

    @MockBean
    private ApplicationEntityRepository aeRepository;

    @MockBean
    private CechoScu cechoScu;

    @Test
    void createAE_shouldSucceed_whenValidConfiguration() {
        // Given
        ApplicationEntity ae = createTestAE("TEST_PACS", "192.168.1.100", 11112);
        when(aeRepository.existsByAeTitle(any())).thenReturn(false);
        when(aeRepository.save(any())).thenReturn(ae);

        // When
        ApplicationEntity created = aeService.createAE(ae);

        // Then
        assertThat(created.getAeTitle()).isEqualTo("TEST_PACS");
        verify(aeRepository).save(any());
    }

    @Test
    void createAE_shouldFail_whenDuplicateAETitle() {
        // Given
        ApplicationEntity ae = createTestAE("DUPLICATE", "192.168.1.100", 11112);
        when(aeRepository.existsByAeTitle("DUPLICATE")).thenReturn(true);

        // When/Then
        assertThrows(DuplicateAETitleException.class, () -> aeService.createAE(ae));
    }

    @Test
    void testConnection_shouldReturnSuccess_whenEchoSucceeds() {
        // Given
        ApplicationEntity localAE = createTestAE("LOCAL", "localhost", 11112);
        ApplicationEntity remoteAE = createTestAE("REMOTE", "192.168.1.100", 11112);
        remoteAE.setId(1L);
        remoteAE.setAeType(AEType.REMOTE_LEGACY);

        when(aeRepository.findById(1L)).thenReturn(Optional.of(remoteAE));
        when(aeRepository.findLocalAE()).thenReturn(Optional.of(localAE));
        when(cechoScu.echo(any(), any())).thenReturn(true);

        // When
        ConnectionStatus status = aeService.testConnection(1L);

        // Then
        assertThat(status).isEqualTo(ConnectionStatus.SUCCESS);
    }

    private ApplicationEntity createTestAE(String aeTitle, String hostname, int port) {
        ApplicationEntity ae = new ApplicationEntity();
        ae.setAeTitle(aeTitle);
        ae.setHostname(hostname);
        ae.setPort(port);
        ae.setAeType(AEType.REMOTE_LEGACY);
        ae.setEnabled(true);
        ae.setConnectionTimeout(30000);
        ae.setResponseTimeout(60000);
        return ae;
    }
}
```

#### C-FIND Service Tests
```java
@SpringBootTest
class CfindScuTest {

    @Autowired
    private CfindScu cfindScu;

    @Test
    void buildStudyQuery_shouldIncludeAllRequiredReturnKeys() {
        // Given
        QueryAttributeBuilder builder = QueryAttributeBuilder.create()
            .forStudyLevel()
            .withPatientName("DOE*")
            .withModality("CT");

        // When
        Attributes attrs = builder.build();

        // Then
        assertThat(attrs.getString(Tag.QueryRetrieveLevel)).isEqualTo("STUDY");
        assertThat(attrs.contains(Tag.PatientName)).isTrue();
        assertThat(attrs.contains(Tag.StudyInstanceUID)).isTrue();
        assertThat(attrs.contains(Tag.StudyDate)).isTrue();
    }

    @Test
    void buildDateRangeQuery_shouldFormatCorrectly() {
        // Given
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        QueryAttributeBuilder builder = QueryAttributeBuilder.create()
            .forStudyLevel()
            .withStudyDateRange(from, to);

        // When
        Attributes attrs = builder.build();

        // Then
        String dateRange = attrs.getString(Tag.StudyDate);
        assertThat(dateRange).isEqualTo("20240101-20241231");
    }
}
```

### Integration Tests

#### C-ECHO Integration Test
```java
@SpringBootTest
@Testcontainers
class CechoScuIntegrationTest {

    @Container
    static GenericContainer<?> orthanc = new GenericContainer<>("orthancteam/orthanc:24.1.2")
        .withExposedPorts(4242, 8042)
        .withEnv("ORTHANC__DICOM_AET", "ORTHANC")
        .waitingFor(Wait.forHttp("/").forPort(8042));

    @Autowired
    private CechoScu cechoScu;

    @Test
    void echo_shouldSucceed_withOrthanc() {
        // Given
        ApplicationEntity localAE = new ApplicationEntity();
        localAE.setAeTitle("DICOMVIEWER");
        localAE.setConnectionTimeout(30000);
        localAE.setResponseTimeout(60000);

        ApplicationEntity remoteAE = new ApplicationEntity();
        remoteAE.setAeTitle("ORTHANC");
        remoteAE.setHostname(orthanc.getHost());
        remoteAE.setPort(orthanc.getMappedPort(4242));

        // When
        boolean result = cechoScu.echo(localAE, remoteAE);

        // Then
        assertThat(result).isTrue();
    }
}
```

#### C-FIND Integration Test
```java
@SpringBootTest
@Testcontainers
class CfindScuIntegrationTest {

    @Container
    static GenericContainer<?> orthanc = new GenericContainer<>("orthancteam/orthanc:24.1.2")
        .withExposedPorts(4242, 8042)
        .withEnv("ORTHANC__DICOM_AET", "ORTHANC")
        .waitingFor(Wait.forHttp("/").forPort(8042));

    @Autowired
    private CfindScu cfindScu;

    @BeforeAll
    static void uploadTestData() {
        // Upload test DICOM files to Orthanc
        // This would typically use Orthanc's REST API
    }

    @Test
    void findStudies_shouldReturnResults_whenStudiesExist() {
        // Given
        ApplicationEntity localAE = createLocalAE();
        ApplicationEntity remoteAE = createRemoteAE();

        Attributes query = QueryAttributeBuilder.create()
            .forStudyLevel()
            .build();

        // When
        List<Study> studies = cfindScu.findStudies(localAE, remoteAE, query);

        // Then
        assertThat(studies).isNotEmpty();
    }
}
```

### Frontend Tests

#### PACS Configuration Component Test
```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PACSConfiguration } from '../components/PACSConfiguration';
import { pacsService } from '../services/pacsService';

jest.mock('../services/pacsService');

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={queryClient}>
    {children}
  </QueryClientProvider>
);

describe('PACSConfiguration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders list of configured PACS', async () => {
    const mockAEs = [
      {
        id: 1,
        aeTitle: 'TEST_PACS',
        hostname: '192.168.1.100',
        port: 11112,
        aeType: 'REMOTE_LEGACY' as const,
        enabled: true,
      },
    ];
    (pacsService.getAllAEs as jest.Mock).mockResolvedValue(mockAEs);

    render(<PACSConfiguration />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText('TEST_PACS')).toBeInTheDocument();
    });
  });

  it('opens create modal when Add PACS clicked', async () => {
    (pacsService.getAllAEs as jest.Mock).mockResolvedValue([]);

    render(<PACSConfiguration />, { wrapper });

    await waitFor(() => {
      fireEvent.click(screen.getByText('Add PACS'));
    });

    expect(screen.getByText('Add New PACS')).toBeInTheDocument();
  });

  it('calls testConnection when Test button clicked', async () => {
    const mockAEs = [
      {
        id: 1,
        aeTitle: 'TEST_PACS',
        hostname: '192.168.1.100',
        port: 11112,
        aeType: 'REMOTE_LEGACY' as const,
        enabled: true,
      },
    ];
    (pacsService.getAllAEs as jest.Mock).mockResolvedValue(mockAEs);
    (pacsService.testConnection as jest.Mock).mockResolvedValue({
      aeId: 1,
      status: 'SUCCESS',
      success: true,
    });

    render(<PACSConfiguration />, { wrapper });

    await waitFor(() => {
      fireEvent.click(screen.getByText('Test'));
    });

    expect(pacsService.testConnection).toHaveBeenCalledWith(1);
  });
});
```

### Manual Testing Checklist

#### C-ECHO Testing
- [ ] Test C-ECHO to Orthanc test PACS
- [ ] Test C-ECHO to production-like PACS (if available)
- [ ] Verify timeout behavior with unreachable host
- [ ] Verify error handling with wrong AE title
- [ ] Verify UI updates after echo test

#### C-FIND Testing
- [ ] Query with no filters returns all studies
- [ ] Query by patient name (exact match)
- [ ] Query by patient name (wildcard: "DOE*")
- [ ] Query by patient ID
- [ ] Query by study date (single date)
- [ ] Query by date range
- [ ] Query by modality
- [ ] Query combining multiple filters
- [ ] Verify series-level query for specific study
- [ ] Verify instance-level query for specific series

#### C-MOVE/C-STORE Testing
- [ ] Initiate C-MOVE for entire study
- [ ] Initiate C-MOVE for single series
- [ ] Verify progress tracking during retrieval
- [ ] Verify instances stored correctly
- [ ] Verify metadata extracted and stored
- [ ] Test with multi-frame images
- [ ] Test with compressed transfer syntaxes
- [ ] Verify storage cleanup works

---

## Deliverables Checklist

### Documentation
- [x] Phase 2 Implementation Guide (this document)
- [ ] Updated API documentation (OpenAPI/Swagger)
- [ ] DICOM network configuration guide
- [ ] Troubleshooting guide for PACS connectivity

### Backend Code Deliverables
- [ ] Application Entity model and repository
- [ ] AE Service with CRUD operations
- [ ] AE REST Controller
- [ ] C-ECHO SCU implementation
- [ ] C-FIND SCU implementation
- [ ] Query Attribute Builder
- [ ] C-MOVE SCU implementation
- [ ] C-STORE SCP implementation
- [ ] Storage Service
- [ ] Stored Instance model and repository
- [ ] Unified Query Service
- [ ] Exception classes for DICOM operations

### Frontend Code Deliverables
- [ ] PACS configuration types
- [ ] PACS service API client
- [ ] PACS Configuration component
- [ ] AE Form Modal component
- [ ] Connection status indicators
- [ ] Retrieval progress component

### Infrastructure
- [ ] Docker Compose updated with local storage volume
- [ ] Database migration scripts for new tables
- [ ] Configuration properties for DICOM network
- [ ] Logging configuration for DICOM operations

### Testing
- [ ] Backend unit tests (> 75% coverage)
- [ ] Integration tests with Orthanc
- [ ] Frontend component tests
- [ ] Manual testing completed

---

## Next Steps After Phase 2

Upon completing Phase 2, the following capabilities will be ready for Phase 3:

1. **Unified Query Interface** - Enables querying from any PACS type
2. **Local Storage** - Foundation for caching and offline viewing
3. **DICOM Network Skills** - Ready for advanced operations (MWL, MPPS)
4. **Robust Error Handling** - Patterns established for all DICOM operations

Continue to [Phase 3: Core Viewer Features](./ROADMAP.md#phase-3-core-viewer-features-weeks-11-16) in the main roadmap.

---

## Appendix: Configuration Reference

### Application Properties

```properties
# DICOM Network Configuration
dicom.local.aetitle=DICOMVIEWER
dicom.local.port=11112
dicom.local.description=Java DICOM Viewer Local AE

# Storage Configuration
dicom.storage.path=/var/dicom/storage
dicom.storage.structure=hierarchical
dicom.storage.max-size-gb=50
dicom.storage.cleanup-enabled=true
dicom.storage.cleanup-threshold-percent=90

# C-STORE SCP Configuration
dicom.store-scp.auto-start=true
dicom.store-scp.max-associations=20

# Connection Defaults
dicom.connection.timeout=30000
dicom.connection.response-timeout=60000
dicom.connection.idle-timeout=60000

# Query Caching
dicom.query.cache-enabled=true
dicom.query.cache-ttl-seconds=300
```

### Database Schema

```sql
-- Application Entities table
CREATE TABLE application_entities (
    id BIGSERIAL PRIMARY KEY,
    ae_title VARCHAR(16) NOT NULL UNIQUE,
    hostname VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    ae_type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    dicomweb_url VARCHAR(500),
    query_retrieve_level VARCHAR(10) DEFAULT 'STUDY',
    is_default BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    connection_timeout INTEGER DEFAULT 30000,
    response_timeout INTEGER DEFAULT 60000,
    max_associations INTEGER DEFAULT 10,
    last_echo_status VARCHAR(20),
    last_echo_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Stored Instances table
CREATE TABLE stored_instances (
    id BIGSERIAL PRIMARY KEY,
    sop_instance_uid VARCHAR(64) NOT NULL UNIQUE,
    sop_class_uid VARCHAR(64),
    study_instance_uid VARCHAR(64) NOT NULL,
    series_instance_uid VARCHAR(64) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    transfer_syntax_uid VARCHAR(64),
    patient_id VARCHAR(64),
    patient_name VARCHAR(255),
    study_date VARCHAR(8),
    modality VARCHAR(16),
    instance_number INTEGER,
    rows INTEGER,
    columns INTEGER,
    stored_at TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP
);

CREATE INDEX idx_stored_study_uid ON stored_instances(study_instance_uid);
CREATE INDEX idx_stored_series_uid ON stored_instances(series_instance_uid);
CREATE INDEX idx_stored_patient_id ON stored_instances(patient_id);
```

---

## Glossary

| Term | Definition |
|------|------------|
| **AE (Application Entity)** | A DICOM node identified by an AE Title, hostname, and port |
| **AE Title** | A unique identifier (max 16 characters) for a DICOM application |
| **C-ECHO** | DICOM Verification Service - tests connectivity between DICOM nodes |
| **C-FIND** | DICOM Query Service - searches for studies/series/instances |
| **C-MOVE** | DICOM Retrieve Service - requests images be sent to a destination |
| **C-STORE** | DICOM Storage Service - receives and stores DICOM instances |
| **SCU (Service Class User)** | The initiator/client of a DICOM operation |
| **SCP (Service Class Provider)** | The responder/server of a DICOM operation |
| **Transfer Syntax** | Encoding format for DICOM data (e.g., compressed, uncompressed) |
| **SOP Class** | Defines the type of DICOM object (e.g., CT Image, MR Image) |
| **Study Root Q/R** | Query/Retrieve model that starts queries at the study level |
| **Patient Root Q/R** | Query/Retrieve model that starts queries at the patient level |
