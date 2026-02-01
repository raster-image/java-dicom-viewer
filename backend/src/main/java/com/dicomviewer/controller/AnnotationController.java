package com.dicomviewer.controller;

import com.dicomviewer.model.entity.Annotation;
import com.dicomviewer.service.AnnotationService;
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
 * REST controller for managing DICOM image annotations.
 */
@RestController
@RequestMapping("/api/annotations")
@Tag(name = "Annotations", description = "CRUD operations for DICOM image annotations")
public class AnnotationController {

    private final AnnotationService annotationService;

    @Autowired
    public AnnotationController(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @PostMapping
    @Operation(summary = "Create a new annotation")
    public ResponseEntity<Annotation> create(@RequestBody Annotation annotation) {
        Annotation created = annotationService.create(annotation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an annotation by ID")
    public ResponseEntity<Annotation> getById(@PathVariable UUID id) {
        Optional<Annotation> annotation = annotationService.getById(id);
        return annotation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an annotation")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Annotation annotation) {
        try {
            Annotation updated = annotationService.update(id, annotation);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an annotation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        annotationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Get all annotations for a study")
    public ResponseEntity<Map<String, Object>> getByStudy(
            @PathVariable String studyInstanceUid,
            @RequestParam(required = false, defaultValue = "false") boolean visibleOnly) {
        
        List<Annotation> annotations = visibleOnly
                ? annotationService.getVisibleByStudy(studyInstanceUid)
                : annotationService.getByStudy(studyInstanceUid);
        
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", annotations.size(),
            "annotations", annotations
        ));
    }

    @GetMapping("/series/{seriesInstanceUid}")
    @Operation(summary = "Get all annotations for a series")
    public ResponseEntity<Map<String, Object>> getBySeries(@PathVariable String seriesInstanceUid) {
        List<Annotation> annotations = annotationService.getBySeries(seriesInstanceUid);
        return ResponseEntity.ok(Map.of(
            "seriesInstanceUid", seriesInstanceUid,
            "count", annotations.size(),
            "annotations", annotations
        ));
    }

    @GetMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Get all annotations for an instance")
    public ResponseEntity<Map<String, Object>> getByInstance(
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false) Integer frameIndex) {
        
        List<Annotation> annotations = frameIndex != null
                ? annotationService.getByInstanceAndFrame(sopInstanceUid, frameIndex)
                : annotationService.getByInstance(sopInstanceUid);
        
        return ResponseEntity.ok(Map.of(
            "sopInstanceUid", sopInstanceUid,
            "frameIndex", frameIndex != null ? frameIndex : "all",
            "count", annotations.size(),
            "annotations", annotations
        ));
    }

    @PostMapping("/{id}/toggle-visibility")
    @Operation(summary = "Toggle visibility of an annotation")
    public ResponseEntity<?> toggleVisibility(@PathVariable UUID id) {
        try {
            Annotation annotation = annotationService.toggleVisibility(id);
            return ResponseEntity.ok(annotation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/toggle-lock")
    @Operation(summary = "Toggle lock state of an annotation")
    public ResponseEntity<?> toggleLock(@PathVariable UUID id) {
        try {
            Annotation annotation = annotationService.toggleLock(id);
            return ResponseEntity.ok(annotation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Delete all annotations for a study")
    public ResponseEntity<Void> deleteByStudy(@PathVariable String studyInstanceUid) {
        annotationService.deleteByStudy(studyInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/series/{seriesInstanceUid}")
    @Operation(summary = "Delete all annotations for a series")
    public ResponseEntity<Void> deleteBySeries(@PathVariable String seriesInstanceUid) {
        annotationService.deleteBySeries(seriesInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Delete all annotations for an instance")
    public ResponseEntity<Void> deleteByInstance(@PathVariable String sopInstanceUid) {
        annotationService.deleteByInstance(sopInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}/count")
    @Operation(summary = "Count annotations for a study")
    public ResponseEntity<Map<String, Object>> countByStudy(@PathVariable String studyInstanceUid) {
        long count = annotationService.countByStudy(studyInstanceUid);
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", count
        ));
    }
}
