package com.dicomviewer.controller;

import com.dicomviewer.model.EchoResultDTO;
import com.dicomviewer.model.entity.ApplicationEntity;
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
@CrossOrigin(origins = "*")
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
    public ResponseEntity<EchoResultDTO> testConnection(@PathVariable Long id) {
        EchoResultDTO result = aeService.testConnectionDetailed(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-all")
    @Operation(summary = "Test connectivity to all enabled remote AEs")
    public ResponseEntity<Map<String, String>> testAllConnections() {
        aeService.testAllConnections();
        return ResponseEntity.ok(Map.of("message", "Connection tests initiated"));
    }
}
