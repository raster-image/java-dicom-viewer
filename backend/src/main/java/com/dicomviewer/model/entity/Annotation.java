package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an annotation on a DICOM image.
 * Supports text labels, arrows, markers, and shape annotations.
 */
@Entity
@Table(name = "annotations", indexes = {
    @Index(name = "idx_annotation_study", columnList = "study_instance_uid"),
    @Index(name = "idx_annotation_series", columnList = "series_instance_uid"),
    @Index(name = "idx_annotation_sop", columnList = "sop_instance_uid"),
    @Index(name = "idx_annotation_user", columnList = "created_by")
})
public class Annotation {

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

    @Column(name = "annotation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AnnotationType annotationType;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "points_json", columnDefinition = "TEXT", nullable = false)
    private String pointsJson;

    @Column(name = "style_json", columnDefinition = "TEXT")
    private String styleJson;

    @Column(name = "color")
    private String color;

    @Column(name = "font_size")
    private Integer fontSize;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum AnnotationType {
        TEXT,
        ARROW,
        MARKER,
        LINE,
        RECTANGLE,
        ELLIPSE,
        POLYLINE
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

    public AnnotationType getAnnotationType() { return annotationType; }
    public void setAnnotationType(AnnotationType annotationType) { 
        this.annotationType = annotationType; 
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPointsJson() { return pointsJson; }
    public void setPointsJson(String pointsJson) { this.pointsJson = pointsJson; }

    public String getStyleJson() { return styleJson; }
    public void setStyleJson(String styleJson) { this.styleJson = styleJson; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
