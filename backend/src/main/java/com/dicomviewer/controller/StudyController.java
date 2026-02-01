package com.dicomviewer.controller;

import com.dicomviewer.dicom.network.CMoveService;
import com.dicomviewer.service.PacsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for study operations.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Studies", description = "Query and retrieve DICOM studies")
public class StudyController {

    private final PacsService pacsService;

    @Autowired
    public StudyController(PacsService pacsService) {
        this.pacsService = pacsService;
    }

    @GetMapping("/studies")
    @Operation(summary = "Query studies from a PACS")
    public ResponseEntity<Map<String, Object>> queryStudies(
            @RequestParam UUID pacsId,
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String studyDate,
            @RequestParam(required = false) String modality,
            @RequestParam(required = false) String accessionNumber,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        Map<String, String> queryParams = new HashMap<>();
        if (patientId != null) queryParams.put("PatientID", patientId);
        if (patientName != null) queryParams.put("PatientName", patientName);
        if (studyDate != null) queryParams.put("StudyDate", studyDate);
        if (modality != null) queryParams.put("ModalitiesInStudy", modality);
        if (accessionNumber != null) queryParams.put("AccessionNumber", accessionNumber);
        queryParams.put("limit", String.valueOf(limit));
        queryParams.put("offset", String.valueOf(offset));

        try {
            List<Map<String, Object>> studies = pacsService.queryStudies(pacsId, queryParams);
            return ResponseEntity.ok(Map.of(
                "total", studies.size(),
                "offset", offset,
                "limit", limit,
                "studies", studies
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "QUERY_FAILED",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/studies/{studyInstanceUid}/series")
    @Operation(summary = "Query series for a study")
    public ResponseEntity<Map<String, Object>> querySeries(
            @PathVariable String studyInstanceUid,
            @RequestParam UUID pacsId) {

        try {
            List<Map<String, Object>> series = pacsService.querySeries(pacsId, studyInstanceUid);
            return ResponseEntity.ok(Map.of("series", series));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "QUERY_FAILED",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances")
    @Operation(summary = "Query instances for a series")
    public ResponseEntity<Map<String, Object>> queryInstances(
            @PathVariable String studyInstanceUid,
            @PathVariable String seriesInstanceUid,
            @RequestParam UUID pacsId) {

        try {
            List<Map<String, Object>> instances = pacsService.queryInstances(pacsId, studyInstanceUid, seriesInstanceUid);
            return ResponseEntity.ok(Map.of("instances", instances));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "QUERY_FAILED",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/studies/{studyInstanceUid}/retrieve")
    @Operation(summary = "Retrieve a study from a legacy PACS using C-MOVE")
    public ResponseEntity<Map<String, Object>> retrieveStudy(
            @PathVariable String studyInstanceUid,
            @RequestParam UUID pacsId,
            @RequestParam String destinationAe) {

        try {
            CMoveService.MoveResult result = pacsService.retrieveStudy(pacsId, studyInstanceUid, destinationAe);
            return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "completedSuboperations", result.getCompletedSuboperations(),
                "failedSuboperations", result.getFailedSuboperations(),
                "warningSuboperations", result.getWarningSuboperations(),
                "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "RETRIEVE_FAILED",
                "message", e.getMessage()
            ));
        }
    }
}
