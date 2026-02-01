package com.rasterimage.dicomviewer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DicomService {

    @Value("${dicom.upload.directory:./uploads}")
    private String uploadDirectory;

    public Map<String, String> processDicomFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the file
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("DICOM file saved: {}", filePath);

        // Return basic file information
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        metadata.put("fileSize", String.format("%.2f KB", file.getSize() / 1024.0));
        metadata.put("contentType", file.getContentType());
        metadata.put("uploadPath", filePath.toString());
        metadata.put("status", "File uploaded successfully");
        metadata.put("note", "DICOM metadata extraction requires dcm4che library. Please add it to dependencies.");

        log.info("Processed file: {}", fileName);

        return metadata;
    }
}
