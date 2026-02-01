package com.rasterimage.dicomviewer.controller;

import com.rasterimage.dicomviewer.service.DicomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomController {

    private final DicomService dicomService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDicomFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".dcm")) {
                response.put("success", false);
                response.put("message", "Invalid file format. Please upload a .dcm file");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, String> metadata = dicomService.processDicomFile(file);
            
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("metadata", metadata);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing DICOM file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "DICOM Viewer");
        return ResponseEntity.ok(response);
    }
}
