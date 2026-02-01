package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a measurement on a DICOM image.
 * Supports length, angle, and ROI measurements.
 */
@Entity
@Table(name = "measurements", indexes = {
    @Index(name = "idx_measurement_study", columnList = "study_instance_uid"),
    @Index(name = "idx_measurement_series", columnList = "series_instance_uid"),
    @Index(name = "idx_measurement_sop", columnList = "sop_instance_uid"),
    @Index(name = "idx_measurement_user", columnList = "created_by")
})
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "study_instance_uid", nullable = false)
    private String studyInstanceUid;

    @Column(name = "series_instance_uid", nullable = false)
    private String seriesInstanceUid;

    @Column(name = "sop_instance_uid", nullable = false)
    private String sopInstanceUid;

    @Column(name = "image_id")
    private String imageId;

    @Column(name = "frame_index")
    private Integer frameIndex;

    @Column(name = "measurement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MeasurementType measurementType;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "label")
    private String label;

    @Column(name = "value")
    private Double value;

    @Column(name = "unit")
    private String unit;

    @Column(name = "points_json", columnDefinition = "TEXT", nullable = false)
    private String pointsJson;

    @Column(name = "roi_stats_json", columnDefinition = "TEXT")
    private String roiStatsJson;

    @Column(name = "color")
    private String color;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum MeasurementType {
        LENGTH,
        ANGLE,
        COBB_ANGLE,
        RECTANGLE_ROI,
        ELLIPSE_ROI,
        POLYGON_ROI,
        FREEHAND_ROI,
        BIDIRECTIONAL,
        PROBE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStudyInstanceUid() { return studyInstanceUid; }
    public void setStudyInstanceUid(String studyInstanceUid) { 
        this.studyInstanceUid = studyInstanceUid; 
    }

    public String getSeriesInstanceUid() { return seriesInstanceUid; }
    public void setSeriesInstanceUid(String seriesInstanceUid) { 
        this.seriesInstanceUid = seriesInstanceUid; 
    }

    public String getSopInstanceUid() { return sopInstanceUid; }
    public void setSopInstanceUid(String sopInstanceUid) { 
        this.sopInstanceUid = sopInstanceUid; 
    }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public Integer getFrameIndex() { return frameIndex; }
    public void setFrameIndex(Integer frameIndex) { this.frameIndex = frameIndex; }

    public MeasurementType getMeasurementType() { return measurementType; }
    public void setMeasurementType(MeasurementType measurementType) { 
        this.measurementType = measurementType; 
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getPointsJson() { return pointsJson; }
    public void setPointsJson(String pointsJson) { this.pointsJson = pointsJson; }

    public String getRoiStatsJson() { return roiStatsJson; }
    public void setRoiStatsJson(String roiStatsJson) { this.roiStatsJson = roiStatsJson; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
