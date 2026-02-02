package com.dicomviewer.service;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import com.dicomviewer.repository.ApplicationEntityRepository;
import com.dicomviewer.dicom.network.CEchoService;
import com.dicomviewer.model.EchoResultDTO;
import com.dicomviewer.model.PacsConfiguration;
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
    private final CEchoService cechoService;

    public ApplicationEntityService(ApplicationEntityRepository aeRepository, CEchoService cechoService) {
        this.aeRepository = aeRepository;
        this.cechoService = cechoService;
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
    public EchoResultDTO testConnectionDetailed(Long aeId) {
        ApplicationEntity ae = aeRepository.findById(aeId)
            .orElseThrow(() -> new AENotFoundException("AE with id " + aeId + " not found"));

        if (ae.getAeType() == AEType.LOCAL) {
            throw new IllegalArgumentException("Cannot test connection to local AE");
        }

        log.info("Testing connection to AE: {} at {}:{}", 
            ae.getAeTitle(), ae.getHostname(), ae.getPort());

        ConnectionStatus status;
        long responseTimeMs = 0;
        String message;

        try {
            // Convert ApplicationEntity to PacsConfiguration for the CEchoService
            PacsConfiguration pacsConfig = new PacsConfiguration();
            pacsConfig.setAeTitle(ae.getAeTitle());
            pacsConfig.setHost(ae.getHostname());
            pacsConfig.setPort(ae.getPort());
            
            CEchoService.EchoResult result = cechoService.echo(pacsConfig);
            status = result.isSuccess() ? ConnectionStatus.SUCCESS : ConnectionStatus.FAILED;
            responseTimeMs = result.getResponseTimeMs();
            message = result.getMessage();
        } catch (Exception e) {
            log.error("C-ECHO failed to {}: {}", ae.getAeTitle(), e.getMessage());
            status = ConnectionStatus.FAILED;
            message = e.getMessage();
        }

        // Update AE with echo status
        ae.setLastEchoStatus(status);
        ae.setLastEchoTime(Instant.now());
        aeRepository.save(ae);

        return new EchoResultDTO(
            ae.getId(),
            ae.getAeTitle(),
            ae.getHostname(),
            ae.getPort(),
            status,
            status == ConnectionStatus.SUCCESS,
            responseTimeMs,
            message
        );
    }

    /**
     * Test connectivity to a remote AE using C-ECHO (simple version).
     */
    public ConnectionStatus testConnection(Long aeId) {
        return testConnectionDetailed(aeId).getStatus();
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
