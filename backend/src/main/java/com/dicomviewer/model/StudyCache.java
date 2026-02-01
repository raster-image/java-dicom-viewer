package com.dicomviewer.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a cached DICOM study.
 */
@Entity
@Table(name = "study_cache")
public class StudyCache {

    @Id
    @Column(name = "study_instance_uid", length = 64)
    private String studyInstanceUid;

    @Column(name = "patient_id", length = 64)
    private String patientId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_birth_date")
    private LocalDate patientBirthDate;

    @Column(name = "patient_sex", length = 1)
    private String patientSex;

    @Column(name = "study_date")
    private LocalDate studyDate;

    @Column(name = "study_time", length = 14)
    private String studyTime;

    @Column(name = "study_description")
    private String studyDescription;

    @Column(length = 16)
    private String modality;

    @Column(name = "accession_number", length = 16)
    private String accessionNumber;

    @Column(name = "referring_physician_name")
    private String referringPhysicianName;

    @Column(name = "number_of_series")
    private Integer numberOfSeries;

    @Column(name = "number_of_instances")
    private Integer numberOfInstances;

    @Column(name = "source_pacs_id")
    private UUID sourcePacsId;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    // Constructors
    public StudyCache() {
    }

    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public void setStudyInstanceUid(String studyInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(LocalDate patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public LocalDate getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(LocalDate studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public Integer getNumberOfSeries() {
        return numberOfSeries;
    }

    public void setNumberOfSeries(Integer numberOfSeries) {
        this.numberOfSeries = numberOfSeries;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public UUID getSourcePacsId() {
        return sourcePacsId;
    }

    public void setSourcePacsId(UUID sourcePacsId) {
        this.sourcePacsId = sourcePacsId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }
}
