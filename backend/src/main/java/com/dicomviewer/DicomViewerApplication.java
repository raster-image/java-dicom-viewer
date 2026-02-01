package com.dicomviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DICOM Viewer.
 * <p>
 * This application provides:
 * - DICOMweb services (WADO-RS, QIDO-RS, STOW-RS)
 * - Legacy PACS support (C-FIND, C-MOVE, C-STORE)
 * - Study management and viewing capabilities
 */
@SpringBootApplication
@EnableAsync
public class DicomViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DicomViewerApplication.class, args);
    }
}
