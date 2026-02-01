package com.dicomviewer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a key image marked by a user.
 * Key images are significant images in a study that are flagged for attention.
 */
@Entity
@Table(name = "key_images", indexes = {
    @Index(name = "idx_key_image_study", columnList = "study_instance_uid"),
    @Index(name = "idx_key_image_series", columnList = "series_instance_uid"),
    @Index(name = "idx_key_image_sop", columnList = "sop_instance_uid"),
    @Index(name = "idx_key_image_user", columnList = "created_by")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_key_image_sop_frame", 
        columnNames = {"sop_instance_uid", "frame_index"})
})
public class KeyImage {

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

    @Column(name = "instance_number")
    private Integer instanceNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "window_width")
    private Double windowWidth;

    @Column(name = "window_center")
    private Double windowCenter;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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

    public Integer getInstanceNumber() { return instanceNumber; }
    public void setInstanceNumber(Integer instanceNumber) { this.instanceNumber = instanceNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getWindowWidth() { return windowWidth; }
    public void setWindowWidth(Double windowWidth) { this.windowWidth = windowWidth; }

    public Double getWindowCenter() { return windowCenter; }
    public void setWindowCenter(Double windowCenter) { this.windowCenter = windowCenter; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
