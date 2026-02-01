package com.dicomviewer.controller;

import com.dicomviewer.model.entity.Measurement;
import com.dicomviewer.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing DICOM image measurements.
 */
@RestController
@RequestMapping("/api/measurements")
@Tag(name = "Measurements", description = "CRUD operations for DICOM image measurements")
public class MeasurementController {

    private final MeasurementService measurementService;

    @Autowired
    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping
    @Operation(summary = "Create a new measurement")
    public ResponseEntity<Measurement> create(@RequestBody Measurement measurement) {
        Measurement created = measurementService.create(measurement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a measurement by ID")
    public ResponseEntity<Measurement> getById(@PathVariable UUID id) {
        Optional<Measurement> measurement = measurementService.getById(id);
        return measurement.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a measurement")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Measurement measurement) {
        try {
            Measurement updated = measurementService.update(id, measurement);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a measurement")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        measurementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Get all measurements for a study")
    public ResponseEntity<Map<String, Object>> getByStudy(
            @PathVariable String studyInstanceUid,
            @RequestParam(required = false, defaultValue = "false") boolean visibleOnly) {
        
        List<Measurement> measurements = visibleOnly
                ? measurementService.getVisibleByStudy(studyInstanceUid)
                : measurementService.getByStudy(studyInstanceUid);
        
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", measurements.size(),
            "measurements", measurements
        ));
    }

    @GetMapping("/series/{seriesInstanceUid}")
    @Operation(summary = "Get all measurements for a series")
    public ResponseEntity<Map<String, Object>> getBySeries(@PathVariable String seriesInstanceUid) {
        List<Measurement> measurements = measurementService.getBySeries(seriesInstanceUid);
        return ResponseEntity.ok(Map.of(
            "seriesInstanceUid", seriesInstanceUid,
            "count", measurements.size(),
            "measurements", measurements
        ));
    }

    @GetMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Get all measurements for an instance")
    public ResponseEntity<Map<String, Object>> getByInstance(
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false) Integer frameIndex) {
        
        List<Measurement> measurements = frameIndex != null
                ? measurementService.getByInstanceAndFrame(sopInstanceUid, frameIndex)
                : measurementService.getByInstance(sopInstanceUid);
        
        return ResponseEntity.ok(Map.of(
            "sopInstanceUid", sopInstanceUid,
            "frameIndex", frameIndex != null ? frameIndex : "all",
            "count", measurements.size(),
            "measurements", measurements
        ));
    }

    @PostMapping("/{id}/toggle-visibility")
    @Operation(summary = "Toggle visibility of a measurement")
    public ResponseEntity<?> toggleVisibility(@PathVariable UUID id) {
        try {
            Measurement measurement = measurementService.toggleVisibility(id);
            return ResponseEntity.ok(measurement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Delete all measurements for a study")
    public ResponseEntity<Void> deleteByStudy(@PathVariable String studyInstanceUid) {
        measurementService.deleteByStudy(studyInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/series/{seriesInstanceUid}")
    @Operation(summary = "Delete all measurements for a series")
    public ResponseEntity<Void> deleteBySeries(@PathVariable String seriesInstanceUid) {
        measurementService.deleteBySeries(seriesInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Delete all measurements for an instance")
    public ResponseEntity<Void> deleteByInstance(@PathVariable String sopInstanceUid) {
        measurementService.deleteByInstance(sopInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}/count")
    @Operation(summary = "Count measurements for a study")
    public ResponseEntity<Map<String, Object>> countByStudy(@PathVariable String studyInstanceUid) {
        long count = measurementService.countByStudy(studyInstanceUid);
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", count
        ));
    }
}
