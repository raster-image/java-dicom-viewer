package com.dicomviewer.controller;

import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.service.PacsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for PACS configuration management.
 */
@RestController
@RequestMapping("/api/pacs")
@Tag(name = "PACS Configuration", description = "Manage PACS connections and settings")
public class PacsController {

    private final PacsService pacsService;

    @Autowired
    public PacsController(PacsService pacsService) {
        this.pacsService = pacsService;
    }

    @GetMapping
    @Operation(summary = "List all PACS configurations")
    public ResponseEntity<Map<String, Object>> listPacs() {
        List<PacsConfiguration> configurations = pacsService.getActivePacsConfigurations();
        return ResponseEntity.ok(Map.of("configurations", configurations));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific PACS configuration")
    public ResponseEntity<PacsConfiguration> getPacs(@PathVariable UUID id) {
        return pacsService.getPacsConfiguration(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new PACS configuration")
    public ResponseEntity<PacsConfiguration> createPacs(@RequestBody PacsConfiguration config) {
        PacsConfiguration created = pacsService.createPacsConfiguration(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing PACS configuration")
    public ResponseEntity<PacsConfiguration> updatePacs(@PathVariable UUID id, @RequestBody PacsConfiguration config) {
        try {
            PacsConfiguration updated = pacsService.updatePacsConfiguration(id, config);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a PACS configuration")
    public ResponseEntity<Void> deletePacs(@PathVariable UUID id) {
        pacsService.deletePacsConfiguration(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test connectivity to a PACS (C-ECHO for legacy, query for DICOMweb)")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable UUID id) {
        try {
            Map<String, Object> result = pacsService.testConnection(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
