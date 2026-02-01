package com.dicomviewer.service;

import com.dicomviewer.model.entity.StoredInstance;
import com.dicomviewer.repository.StoredInstanceRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/**
 * Service for managing local DICOM storage.
 */
@Service
@Transactional
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Value("${dicom.storage.path:/var/dicom/storage}")
    private String storagePath;

    @Value("${dicom.storage.structure:hierarchical}")
    private String storageStructure;

    private final StoredInstanceRepository instanceRepository;

    public StorageService(StoredInstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    /**
     * Store a received DICOM instance.
     */
    public StoredInstance storeInstance(File sourceFile, Attributes dataset, 
                                        Attributes fileMetaInfo) throws IOException {
        String studyUID = dataset.getString(Tag.StudyInstanceUID);
        String seriesUID = dataset.getString(Tag.SeriesInstanceUID);
        String sopInstanceUID = dataset.getString(Tag.SOPInstanceUID);

        // Check if already stored
        if (instanceRepository.existsBySopInstanceUid(sopInstanceUID)) {
            log.debug("Instance already stored: {}", sopInstanceUID);
            return instanceRepository.findBySopInstanceUid(sopInstanceUID).orElse(null);
        }

        // Calculate storage path
        Path targetPath = calculateStoragePath(studyUID, seriesUID, sopInstanceUID);
        Files.createDirectories(targetPath.getParent());

        // Move file to storage location
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Create database entry
        StoredInstance instance = new StoredInstance();
        instance.setSopInstanceUid(sopInstanceUID);
        instance.setSopClassUid(dataset.getString(Tag.SOPClassUID));
        instance.setStudyInstanceUid(studyUID);
        instance.setSeriesInstanceUid(seriesUID);
        instance.setFilePath(targetPath.toString());
        instance.setFileSize(Files.size(targetPath));
        instance.setTransferSyntaxUid(fileMetaInfo.getString(Tag.TransferSyntaxUID));
        
        // Extract additional metadata
        instance.setPatientId(dataset.getString(Tag.PatientID));
        instance.setPatientName(dataset.getString(Tag.PatientName));
        instance.setStudyDate(dataset.getString(Tag.StudyDate));
        instance.setModality(dataset.getString(Tag.Modality));
        instance.setInstanceNumber(dataset.getInt(Tag.InstanceNumber, 0));
        instance.setRows(dataset.getInt(Tag.Rows, 0));
        instance.setColumns(dataset.getInt(Tag.Columns, 0));
        instance.setStoredAt(Instant.now());

        StoredInstance saved = instanceRepository.save(instance);
        log.info("Stored instance: {} at {}", sopInstanceUID, targetPath);

        return saved;
    }

    /**
     * Get the file for a stored instance.
     */
    public File getInstanceFile(String sopInstanceUid) {
        StoredInstance instance = instanceRepository.findBySopInstanceUid(sopInstanceUid)
            .orElseThrow(() -> new IllegalArgumentException(
                "Instance not found: " + sopInstanceUid));
        return new File(instance.getFilePath());
    }

    /**
     * Check if an instance exists in local storage.
     */
    public boolean hasInstance(String sopInstanceUid) {
        return instanceRepository.existsBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Delete a stored instance.
     */
    public void deleteInstance(String sopInstanceUid) throws IOException {
        StoredInstance instance = instanceRepository.findBySopInstanceUid(sopInstanceUid)
            .orElseThrow(() -> new IllegalArgumentException(
                "Instance not found: " + sopInstanceUid));
        
        Files.deleteIfExists(Paths.get(instance.getFilePath()));
        instanceRepository.delete(instance);
        
        log.info("Deleted instance: {}", sopInstanceUid);
    }

    /**
     * Delete all instances for a study.
     */
    public void deleteStudy(String studyInstanceUid) throws IOException {
        var instances = instanceRepository.findByStudyInstanceUid(studyInstanceUid);
        for (StoredInstance instance : instances) {
            Files.deleteIfExists(Paths.get(instance.getFilePath()));
        }
        instanceRepository.deleteAll(instances);
        
        // Try to remove empty directories
        Path studyDir = Paths.get(storagePath, studyInstanceUid);
        if (Files.exists(studyDir)) {
            Files.walk(studyDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ignored) {}
                });
        }
        
        log.info("Deleted study: {}", studyInstanceUid);
    }

    /**
     * Get storage statistics.
     */
    public StorageStats getStorageStats() {
        StorageStats stats = new StorageStats();
        stats.setTotalInstances(instanceRepository.count());
        stats.setTotalSize(instanceRepository.sumFileSize());
        stats.setStudyCount(instanceRepository.countDistinctStudies());
        stats.setSeriesCount(instanceRepository.countDistinctSeries());
        return stats;
    }

    /**
     * Calculate storage path based on configuration.
     */
    private Path calculateStoragePath(String studyUID, String seriesUID, 
                                      String sopInstanceUID) {
        if ("flat".equals(storageStructure)) {
            return Paths.get(storagePath, sopInstanceUID + ".dcm");
        } else {
            // Hierarchical: storage/studyUID/seriesUID/sopInstanceUID.dcm
            return Paths.get(storagePath, studyUID, seriesUID, sopInstanceUID + ".dcm");
        }
    }

    /**
     * Storage statistics DTO.
     */
    public static class StorageStats {
        private long totalInstances;
        private long totalSize;
        private long studyCount;
        private long seriesCount;

        // Getters and setters
        public long getTotalInstances() { return totalInstances; }
        public void setTotalInstances(long totalInstances) { this.totalInstances = totalInstances; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public long getStudyCount() { return studyCount; }
        public void setStudyCount(long studyCount) { this.studyCount = studyCount; }
        
        public long getSeriesCount() { return seriesCount; }
        public void setSeriesCount(long seriesCount) { this.seriesCount = seriesCount; }
        
        public String getFormattedSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return (totalSize / 1024) + " KB";
            if (totalSize < 1024 * 1024 * 1024) return (totalSize / (1024 * 1024)) + " MB";
            return (totalSize / (1024 * 1024 * 1024)) + " GB";
        }
    }
}
