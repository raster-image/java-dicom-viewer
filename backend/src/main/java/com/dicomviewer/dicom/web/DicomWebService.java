package com.dicomviewer.dicom.web;

import com.dicomviewer.model.PacsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * Service for DICOMweb operations (QIDO-RS, WADO-RS, STOW-RS).
 * Provides integration with modern PACS systems that support DICOMweb.
 */
@Service
public class DicomWebService {

    private static final Logger log = LoggerFactory.getLogger(DicomWebService.class);

    private final RestTemplate restTemplate;

    public DicomWebService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Query studies using QIDO-RS.
     *
     * @param pacsConfig The PACS configuration with DICOMweb URLs
     * @param queryParams Query parameters
     * @return List of study metadata
     */
    public List<Map<String, Object>> queryStudies(PacsConfiguration pacsConfig, Map<String, String> queryParams) {
        String qidoUrl = pacsConfig.getQidoRsUrl();
        if (qidoUrl == null || qidoUrl.isEmpty()) {
            throw new IllegalArgumentException("QIDO-RS URL not configured for PACS: " + pacsConfig.getName());
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(qidoUrl + "/studies");

        // Add query parameters
        queryParams.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                builder.queryParam(key, value);
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                List.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("QIDO-RS query returned {} studies from {}", response.getBody().size(), pacsConfig.getName());
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("QIDO-RS query failed for {}: {}", pacsConfig.getName(), e.getMessage());
            throw new RuntimeException("QIDO-RS query failed: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Query series using QIDO-RS.
     */
    public List<Map<String, Object>> querySeries(PacsConfiguration pacsConfig, String studyInstanceUid) {
        String qidoUrl = pacsConfig.getQidoRsUrl();
        if (qidoUrl == null || qidoUrl.isEmpty()) {
            throw new IllegalArgumentException("QIDO-RS URL not configured");
        }

        String url = String.format("%s/studies/%s/series", qidoUrl, studyInstanceUid);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("QIDO-RS series query failed: {}", e.getMessage());
            throw new RuntimeException("QIDO-RS series query failed: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Query instances using QIDO-RS.
     */
    public List<Map<String, Object>> queryInstances(PacsConfiguration pacsConfig, String studyInstanceUid, String seriesInstanceUid) {
        String qidoUrl = pacsConfig.getQidoRsUrl();
        if (qidoUrl == null || qidoUrl.isEmpty()) {
            throw new IllegalArgumentException("QIDO-RS URL not configured");
        }

        String url = String.format("%s/studies/%s/series/%s/instances", qidoUrl, studyInstanceUid, seriesInstanceUid);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("QIDO-RS instances query failed: {}", e.getMessage());
            throw new RuntimeException("QIDO-RS instances query failed: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Retrieve a DICOM instance using WADO-RS.
     *
     * @param pacsConfig The PACS configuration
     * @param studyInstanceUid Study Instance UID
     * @param seriesInstanceUid Series Instance UID
     * @param sopInstanceUid SOP Instance UID
     * @return The DICOM file bytes
     */
    public byte[] retrieveInstance(PacsConfiguration pacsConfig, String studyInstanceUid,
                                    String seriesInstanceUid, String sopInstanceUid) {
        String wadoUrl = pacsConfig.getWadoRsUrl();
        if (wadoUrl == null || wadoUrl.isEmpty()) {
            throw new IllegalArgumentException("WADO-RS URL not configured");
        }

        String url = String.format("%s/studies/%s/series/%s/instances/%s",
            wadoUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType("application/dicom")));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Retrieved instance {} ({} bytes)", sopInstanceUid, response.getBody().length);
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("WADO-RS retrieve failed: {}", e.getMessage());
            throw new RuntimeException("WADO-RS retrieve failed: " + e.getMessage(), e);
        }

        return new byte[0];
    }

    /**
     * Retrieve rendered image using WADO-RS.
     */
    public byte[] retrieveRenderedImage(PacsConfiguration pacsConfig, String studyInstanceUid,
                                         String seriesInstanceUid, String sopInstanceUid,
                                         String mediaType) {
        String wadoUrl = pacsConfig.getWadoRsUrl();
        if (wadoUrl == null || wadoUrl.isEmpty()) {
            throw new IllegalArgumentException("WADO-RS URL not configured");
        }

        String url = String.format("%s/studies/%s/series/%s/instances/%s/rendered",
            wadoUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType(mediaType)));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("WADO-RS rendered retrieve failed: {}", e.getMessage());
            throw new RuntimeException("WADO-RS rendered retrieve failed: " + e.getMessage(), e);
        }

        return new byte[0];
    }
}
