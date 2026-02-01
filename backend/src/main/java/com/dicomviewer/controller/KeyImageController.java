package com.dicomviewer.controller;

import com.dicomviewer.model.entity.KeyImage;
import com.dicomviewer.service.KeyImageService;
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
 * REST controller for managing key images.
 */
@RestController
@RequestMapping("/api/key-images")
@Tag(name = "Key Images", description = "Manage key image marking and retrieval")
public class KeyImageController {

    private final KeyImageService keyImageService;

    @Autowired
    public KeyImageController(KeyImageService keyImageService) {
        this.keyImageService = keyImageService;
    }

    @PostMapping
    @Operation(summary = "Create a new key image")
    public ResponseEntity<?> create(@RequestBody KeyImage keyImage) {
        try {
            KeyImage created = keyImageService.create(keyImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "ALREADY_EXISTS",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/toggle")
    @Operation(summary = "Toggle key image status (mark/unmark)")
    public ResponseEntity<Map<String, Object>> toggle(@RequestBody KeyImage keyImage) {
        KeyImage result = keyImageService.toggleKeyImage(keyImage);
        if (result == null) {
            return ResponseEntity.ok(Map.of(
                "action", "removed",
                "isKeyImage", false
            ));
        }
        return ResponseEntity.ok(Map.of(
            "action", "added",
            "isKeyImage", true,
            "keyImage", result
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a key image by ID")
    public ResponseEntity<KeyImage> getById(@PathVariable UUID id) {
        Optional<KeyImage> keyImage = keyImageService.getById(id);
        return keyImage.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a key image")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody KeyImage keyImage) {
        try {
            KeyImage updated = keyImageService.update(id, keyImage);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a key image")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        keyImageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Get all key images for a study")
    public ResponseEntity<Map<String, Object>> getByStudy(
            @PathVariable String studyInstanceUid,
            @RequestParam(required = false) String category) {
        
        List<KeyImage> keyImages = category != null
                ? keyImageService.getByStudyAndCategory(studyInstanceUid, category)
                : keyImageService.getByStudy(studyInstanceUid);
        
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", keyImages.size(),
            "keyImages", keyImages
        ));
    }

    @GetMapping("/series/{seriesInstanceUid}")
    @Operation(summary = "Get all key images for a series")
    public ResponseEntity<Map<String, Object>> getBySeries(@PathVariable String seriesInstanceUid) {
        List<KeyImage> keyImages = keyImageService.getBySeries(seriesInstanceUid);
        return ResponseEntity.ok(Map.of(
            "seriesInstanceUid", seriesInstanceUid,
            "count", keyImages.size(),
            "keyImages", keyImages
        ));
    }

    @GetMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Get all key images for an instance")
    public ResponseEntity<Map<String, Object>> getByInstance(@PathVariable String sopInstanceUid) {
        List<KeyImage> keyImages = keyImageService.getByInstance(sopInstanceUid);
        return ResponseEntity.ok(Map.of(
            "sopInstanceUid", sopInstanceUid,
            "count", keyImages.size(),
            "keyImages", keyImages
        ));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if an image is marked as key image")
    public ResponseEntity<Map<String, Object>> checkKeyImage(
            @RequestParam String sopInstanceUid,
            @RequestParam(required = false, defaultValue = "0") Integer frameIndex) {
        
        boolean isKeyImage = keyImageService.isKeyImage(sopInstanceUid, frameIndex);
        return ResponseEntity.ok(Map.of(
            "sopInstanceUid", sopInstanceUid,
            "frameIndex", frameIndex,
            "isKeyImage", isKeyImage
        ));
    }

    @DeleteMapping("/instance/{sopInstanceUid}")
    @Operation(summary = "Delete key image by instance")
    public ResponseEntity<Void> deleteByInstance(
            @PathVariable String sopInstanceUid,
            @RequestParam(required = false, defaultValue = "0") Integer frameIndex) {
        
        keyImageService.deleteByInstanceAndFrame(sopInstanceUid, frameIndex);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/study/{studyInstanceUid}")
    @Operation(summary = "Delete all key images for a study")
    public ResponseEntity<Void> deleteByStudy(@PathVariable String studyInstanceUid) {
        keyImageService.deleteByStudy(studyInstanceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/study/{studyInstanceUid}/count")
    @Operation(summary = "Count key images for a study")
    public ResponseEntity<Map<String, Object>> countByStudy(@PathVariable String studyInstanceUid) {
        long count = keyImageService.countByStudy(studyInstanceUid);
        return ResponseEntity.ok(Map.of(
            "studyInstanceUid", studyInstanceUid,
            "count", count
        ));
    }
}
