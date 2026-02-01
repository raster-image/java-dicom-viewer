package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a locally stored DICOM instance.
 */
@Entity
@Table(name = "stored_instances", indexes = {
    @Index(name = "idx_sop_instance_uid", columnList = "sop_instance_uid", unique = true),
    @Index(name = "idx_study_instance_uid", columnList = "study_instance_uid"),
    @Index(name = "idx_series_instance_uid", columnList = "series_instance_uid"),
    @Index(name = "idx_patient_id", columnList = "patient_id")
})
public class StoredInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sop_instance_uid", nullable = false, unique = true)
    private String sopInstanceUid;

    @Column(name = "sop_class_uid")
    private String sopClassUid;

    @Column(name = "study_instance_uid", nullable = false)
    private String studyInstanceUid;

    @Column(name = "series_instance_uid", nullable = false)
    private String seriesInstanceUid;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "transfer_syntax_uid")
    private String transferSyntaxUid;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "study_date")
    private String studyDate;

    @Column(name = "modality")
    private String modality;

    @Column(name = "instance_number")
    private Integer instanceNumber;

    @Column(name = "rows")
    private Integer rows;

    @Column(name = "columns")
    private Integer columns;

    @Column(name = "stored_at", nullable = false)
    private Instant storedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSopInstanceUid() { return sopInstanceUid; }
    public void setSopInstanceUid(String sopInstanceUid) { this.sopInstanceUid = sopInstanceUid; }

    public String getSopClassUid() { return sopClassUid; }
    public void setSopClassUid(String sopClassUid) { this.sopClassUid = sopClassUid; }

    public String getStudyInstanceUid() { return studyInstanceUid; }
    public void setStudyInstanceUid(String studyInstanceUid) { 
        this.studyInstanceUid = studyInstanceUid; 
    }

    public String getSeriesInstanceUid() { return seriesInstanceUid; }
    public void setSeriesInstanceUid(String seriesInstanceUid) { 
        this.seriesInstanceUid = seriesInstanceUid; 
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getTransferSyntaxUid() { return transferSyntaxUid; }
    public void setTransferSyntaxUid(String transferSyntaxUid) { 
        this.transferSyntaxUid = transferSyntaxUid; 
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getStudyDate() { return studyDate; }
    public void setStudyDate(String studyDate) { this.studyDate = studyDate; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public Integer getInstanceNumber() { return instanceNumber; }
    public void setInstanceNumber(Integer instanceNumber) { this.instanceNumber = instanceNumber; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }

    public Instant getStoredAt() { return storedAt; }
    public void setStoredAt(Instant storedAt) { this.storedAt = storedAt; }

    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}
