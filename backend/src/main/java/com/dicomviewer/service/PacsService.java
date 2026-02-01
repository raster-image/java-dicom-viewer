package com.dicomviewer.service;

import com.dicomviewer.dicom.network.CEchoService;
import com.dicomviewer.dicom.network.CFindService;
import com.dicomviewer.dicom.network.CMoveService;
import com.dicomviewer.dicom.web.DicomWebService;
import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.repository.PacsConfigurationRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Unified service for PACS operations.
 * Routes requests to appropriate service based on PACS type (DICOMweb or Legacy).
 */
@Service
public class PacsService {

    private static final Logger log = LoggerFactory.getLogger(PacsService.class);

    private final PacsConfigurationRepository pacsConfigRepository;
    private final CEchoService cEchoService;
    private final CFindService cFindService;
    private final CMoveService cMoveService;
    private final DicomWebService dicomWebService;

    @Autowired
    public PacsService(PacsConfigurationRepository pacsConfigRepository,
                       CEchoService cEchoService,
                       CFindService cFindService,
                       CMoveService cMoveService,
                       DicomWebService dicomWebService) {
        this.pacsConfigRepository = pacsConfigRepository;
        this.cEchoService = cEchoService;
        this.cFindService = cFindService;
        this.cMoveService = cMoveService;
        this.dicomWebService = dicomWebService;
    }

    /**
     * Get all active PACS configurations.
     */
    public List<PacsConfiguration> getActivePacsConfigurations() {
        return pacsConfigRepository.findByIsActiveTrue();
    }

    /**
     * Get a PACS configuration by ID.
     */
    public Optional<PacsConfiguration> getPacsConfiguration(UUID id) {
        return pacsConfigRepository.findById(id);
    }

    /**
     * Get a PACS configuration by ID or null if not found.
     */
    public PacsConfiguration getPacs(UUID id) {
        return pacsConfigRepository.findById(id).orElse(null);
    }

    /**
     * Get the first active PACS configuration.
     */
    public PacsConfiguration getFirstActivePacs() {
        List<PacsConfiguration> activePacs = pacsConfigRepository.findByIsActiveTrue();
        return activePacs.isEmpty() ? null : activePacs.get(0);
    }

    /**
     * Create a new PACS configuration.
     */
    public PacsConfiguration createPacsConfiguration(PacsConfiguration config) {
        return pacsConfigRepository.save(config);
    }

    /**
     * Update a PACS configuration.
     */
    public PacsConfiguration updatePacsConfiguration(UUID id, PacsConfiguration config) {
        return pacsConfigRepository.findById(id)
            .map(existing -> {
                existing.setName(config.getName());
                existing.setHost(config.getHost());
                existing.setPort(config.getPort());
                existing.setAeTitle(config.getAeTitle());
                existing.setPacsType(config.getPacsType());
                existing.setWadoRsUrl(config.getWadoRsUrl());
                existing.setQidoRsUrl(config.getQidoRsUrl());
                existing.setStowRsUrl(config.getStowRsUrl());
                existing.setIsActive(config.getIsActive());
                return pacsConfigRepository.save(existing);
            })
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + id));
    }

    /**
     * Delete a PACS configuration.
     */
    public void deletePacsConfiguration(UUID id) {
        pacsConfigRepository.deleteById(id);
    }

    /**
     * Test connectivity to a PACS.
     */
    public Map<String, Object> testConnection(UUID pacsId) {
        PacsConfiguration config = pacsConfigRepository.findById(pacsId)
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + pacsId));

        Map<String, Object> result = new HashMap<>();

        if (config.isLegacy()) {
            CEchoService.EchoResult echoResult = cEchoService.echo(config);
            result.put("success", echoResult.isSuccess());
            result.put("responseTime", echoResult.getResponseTimeMs());
            result.put("message", echoResult.getMessage());
        } else {
            // For DICOMweb, try a simple query
            try {
                dicomWebService.queryStudies(config, Map.of("limit", "1"));
                result.put("success", true);
                result.put("message", "Connection successful");
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", e.getMessage());
            }
        }

        return result;
    }

    /**
     * Query studies from a PACS.
     * Automatically routes to appropriate service based on PACS type.
     */
    public List<Map<String, Object>> queryStudies(UUID pacsId, Map<String, String> queryParams) {
        PacsConfiguration config = pacsConfigRepository.findById(pacsId)
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + pacsId));

        if (config.isDicomWeb()) {
            return dicomWebService.queryStudies(config, queryParams);
        } else {
            // Use C-FIND for legacy PACS
            CFindService.StudyQuery query = new CFindService.StudyQuery();
            query.setPatientId(queryParams.get("PatientID"));
            query.setPatientName(queryParams.get("PatientName"));
            query.setStudyDate(queryParams.get("StudyDate"));
            query.setModality(queryParams.get("ModalitiesInStudy"));
            query.setAccessionNumber(queryParams.get("AccessionNumber"));

            List<Attributes> results = cFindService.findStudies(config, query);
            return convertAttributesToMaps(results);
        }
    }

    /**
     * Query series from a PACS.
     */
    public List<Map<String, Object>> querySeries(UUID pacsId, String studyInstanceUid) {
        PacsConfiguration config = pacsConfigRepository.findById(pacsId)
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + pacsId));

        if (config.isDicomWeb()) {
            return dicomWebService.querySeries(config, studyInstanceUid);
        } else {
            List<Attributes> results = cFindService.findSeries(config, studyInstanceUid);
            return convertAttributesToMaps(results);
        }
    }

    /**
     * Query instances from a PACS.
     */
    public List<Map<String, Object>> queryInstances(UUID pacsId, String studyInstanceUid, String seriesInstanceUid) {
        PacsConfiguration config = pacsConfigRepository.findById(pacsId)
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + pacsId));

        if (config.isDicomWeb()) {
            return dicomWebService.queryInstances(config, studyInstanceUid, seriesInstanceUid);
        } else {
            List<Attributes> results = cFindService.findInstances(config, studyInstanceUid, seriesInstanceUid);
            return convertAttributesToMaps(results);
        }
    }

    /**
     * Retrieve a study from a legacy PACS using C-MOVE.
     */
    public CMoveService.MoveResult retrieveStudy(UUID pacsId, String studyInstanceUid, String destinationAe) {
        PacsConfiguration config = pacsConfigRepository.findById(pacsId)
            .orElseThrow(() -> new RuntimeException("PACS configuration not found: " + pacsId));

        if (!config.isLegacy()) {
            throw new IllegalArgumentException("C-MOVE is only supported for Legacy PACS systems");
        }

        return cMoveService.moveStudy(config, studyInstanceUid, destinationAe);
    }

    /**
     * Convert dcm4che Attributes to Maps for JSON serialization.
     */
    private List<Map<String, Object>> convertAttributesToMaps(List<Attributes> attributesList) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Attributes attrs : attributesList) {
            Map<String, Object> map = new HashMap<>();

            // Patient level
            putIfPresent(map, "PatientID", attrs.getString(Tag.PatientID));
            putIfPresent(map, "PatientName", attrs.getString(Tag.PatientName));
            putIfPresent(map, "PatientBirthDate", attrs.getString(Tag.PatientBirthDate));
            putIfPresent(map, "PatientSex", attrs.getString(Tag.PatientSex));

            // Study level
            putIfPresent(map, "StudyInstanceUID", attrs.getString(Tag.StudyInstanceUID));
            putIfPresent(map, "StudyDate", attrs.getString(Tag.StudyDate));
            putIfPresent(map, "StudyTime", attrs.getString(Tag.StudyTime));
            putIfPresent(map, "StudyDescription", attrs.getString(Tag.StudyDescription));
            putIfPresent(map, "AccessionNumber", attrs.getString(Tag.AccessionNumber));
            putIfPresent(map, "ModalitiesInStudy", attrs.getString(Tag.ModalitiesInStudy));
            putIfPresent(map, "NumberOfStudyRelatedSeries", attrs.getString(Tag.NumberOfStudyRelatedSeries));
            putIfPresent(map, "NumberOfStudyRelatedInstances", attrs.getString(Tag.NumberOfStudyRelatedInstances));

            // Series level
            putIfPresent(map, "SeriesInstanceUID", attrs.getString(Tag.SeriesInstanceUID));
            putIfPresent(map, "SeriesNumber", attrs.getString(Tag.SeriesNumber));
            putIfPresent(map, "SeriesDescription", attrs.getString(Tag.SeriesDescription));
            putIfPresent(map, "Modality", attrs.getString(Tag.Modality));
            putIfPresent(map, "NumberOfSeriesRelatedInstances", attrs.getString(Tag.NumberOfSeriesRelatedInstances));

            // Instance level
            putIfPresent(map, "SOPInstanceUID", attrs.getString(Tag.SOPInstanceUID));
            putIfPresent(map, "SOPClassUID", attrs.getString(Tag.SOPClassUID));
            putIfPresent(map, "InstanceNumber", attrs.getString(Tag.InstanceNumber));

            results.add(map);
        }

        return results;
    }

    private void putIfPresent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }
}
