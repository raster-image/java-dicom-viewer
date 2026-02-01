package com.dicomviewer.controller;

import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.service.PacsService;
import com.dicomviewer.dicom.web.DicomWebService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for WADO-RS proxy operations.
 * Proxies WADO-RS requests to the configured PACS.
 */
@RestController
@RequestMapping("/api/wado")
@Tag(name = "WADO-RS", description = "Web Access to DICOM Objects - RESTful Services")
public class WadoController {

    private static final Logger log = LoggerFactory.getLogger(WadoController.class);

    private final PacsService pacsService;
    private final DicomWebService dicomWebService;

    public WadoController(PacsService pacsService, DicomWebService dicomWebService) {
        this.pacsService = pacsService;
        this.dicomWebService = dicomWebService;
    }

    @GetMapping("/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}")
    @Operation(summary = "Retrieve a DICOM instance")
    public ResponseEntity<byte[]> retrieveInstance(
            @PathVariable String studyInstanceUid,
            @PathVariable String seriesInstanceUid,
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false) UUID pacsId) {

        try {
            PacsConfiguration pacs = findPacs(pacsId);
            if (pacs == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No PACS configuration found".getBytes());
            }

            byte[] dicomData = dicomWebService.retrieveInstance(
                    pacs, studyInstanceUid, seriesInstanceUid, sopInstanceUid);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/dicom"));
            headers.setContentLength(dicomData.length);

            return new ResponseEntity<>(dicomData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to retrieve DICOM instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/frames/{frameNumber}")
    @Operation(summary = "Retrieve a specific frame from a DICOM instance",
               description = "Note: Currently retrieves the entire instance. Frame extraction is performed client-side. "
                           + "For single-frame images, frame 1 returns the image. For multi-frame images, "
                           + "the client (Cornerstone.js) extracts the specified frame from the returned DICOM data.")
    public ResponseEntity<byte[]> retrieveFrame(
            @PathVariable String studyInstanceUid,
            @PathVariable String seriesInstanceUid,
            @PathVariable String sopInstanceUid,
            @PathVariable int frameNumber,
            @RequestParam(required = false) UUID pacsId) {

        try {
            PacsConfiguration pacs = findPacs(pacsId);
            if (pacs == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No PACS configuration found".getBytes());
            }

            // Retrieve the whole instance - Cornerstone.js handles frame extraction client-side
            // This approach is common in DICOMweb implementations for simplicity
            byte[] dicomData = dicomWebService.retrieveInstance(
                    pacs, studyInstanceUid, seriesInstanceUid, sopInstanceUid);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/dicom"));
            headers.setContentLength(dicomData.length);

            return new ResponseEntity<>(dicomData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to retrieve DICOM frame: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/rendered")
    @Operation(summary = "Retrieve a rendered image (JPEG/PNG)")
    public ResponseEntity<byte[]> retrieveRendered(
            @PathVariable String studyInstanceUid,
            @PathVariable String seriesInstanceUid,
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false) UUID pacsId,
            @RequestParam(required = false) Integer windowWidth,
            @RequestParam(required = false) Integer windowCenter,
            @RequestHeader(value = "Accept", defaultValue = "image/jpeg") String accept) {

        try {
            PacsConfiguration pacs = findPacs(pacsId);
            if (pacs == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No PACS configuration found".getBytes());
            }

            String mediaType = accept.contains("image/png") ? "image/png" : "image/jpeg";
            byte[] imageData = dicomWebService.retrieveRenderedImage(
                    pacs, studyInstanceUid, seriesInstanceUid, sopInstanceUid, mediaType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mediaType));
            headers.setContentLength(imageData.length);

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to retrieve rendered image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/thumbnail")
    @Operation(summary = "Retrieve a thumbnail image")
    public ResponseEntity<byte[]> retrieveThumbnail(
            @PathVariable String studyInstanceUid,
            @PathVariable String seriesInstanceUid,
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false) UUID pacsId,
            @RequestParam(defaultValue = "128") int size) {

        try {
            PacsConfiguration pacs = findPacs(pacsId);
            if (pacs == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No PACS configuration found".getBytes());
            }

            // Use rendered endpoint for thumbnail
            byte[] imageData = dicomWebService.retrieveRenderedImage(
                    pacs, studyInstanceUid, seriesInstanceUid, sopInstanceUid, "image/jpeg");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imageData.length);

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to retrieve thumbnail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Find the PACS configuration to use.
     * If pacsId is provided, use that. Otherwise, return the first active PACS.
     */
    private PacsConfiguration findPacs(UUID pacsId) {
        if (pacsId != null) {
            return pacsService.getPacs(pacsId);
        }
        // Return first active PACS if none specified
        return pacsService.getFirstActivePacs();
    }
}
