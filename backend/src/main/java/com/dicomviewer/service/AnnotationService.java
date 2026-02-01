package com.dicomviewer.service;

import com.dicomviewer.model.entity.Annotation;
import com.dicomviewer.repository.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing DICOM image annotations.
 */
@Service
public class AnnotationService {

    private final AnnotationRepository annotationRepository;

    @Autowired
    public AnnotationService(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    /**
     * Create a new annotation.
     */
    @Transactional
    public Annotation create(Annotation annotation) {
        return annotationRepository.save(annotation);
    }

    /**
     * Update an existing annotation.
     */
    @Transactional
    public Annotation update(UUID id, Annotation annotation) {
        Optional<Annotation> existing = annotationRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Annotation not found: " + id);
        }

        Annotation updated = existing.get();
        if (annotation.getText() != null) {
            updated.setText(annotation.getText());
        }
        if (annotation.getPointsJson() != null) {
            updated.setPointsJson(annotation.getPointsJson());
        }
        if (annotation.getStyleJson() != null) {
            updated.setStyleJson(annotation.getStyleJson());
        }
        if (annotation.getColor() != null) {
            updated.setColor(annotation.getColor());
        }
        if (annotation.getFontSize() != null) {
            updated.setFontSize(annotation.getFontSize());
        }
        updated.setVisible(annotation.isVisible());
        updated.setLocked(annotation.isLocked());

        return annotationRepository.save(updated);
    }

    /**
     * Get an annotation by ID.
     */
    public Optional<Annotation> getById(UUID id) {
        return annotationRepository.findById(id);
    }

    /**
     * Get all annotations for a study.
     */
    public List<Annotation> getByStudy(String studyInstanceUid) {
        return annotationRepository.findByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Get all annotations for a series.
     */
    public List<Annotation> getBySeries(String seriesInstanceUid) {
        return annotationRepository.findBySeriesInstanceUid(seriesInstanceUid);
    }

    /**
     * Get all annotations for an instance.
     */
    public List<Annotation> getByInstance(String sopInstanceUid) {
        return annotationRepository.findBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Get annotations for a specific frame.
     */
    public List<Annotation> getByInstanceAndFrame(String sopInstanceUid, Integer frameIndex) {
        return annotationRepository.findBySopInstanceUidAndFrameIndex(sopInstanceUid, frameIndex);
    }

    /**
     * Get visible annotations for a study.
     */
    public List<Annotation> getVisibleByStudy(String studyInstanceUid) {
        return annotationRepository.findByStudyInstanceUidAndVisible(studyInstanceUid, true);
    }

    /**
     * Get annotations by type.
     */
    public List<Annotation> getByType(Annotation.AnnotationType type) {
        return annotationRepository.findByAnnotationType(type);
    }

    /**
     * Delete an annotation.
     */
    @Transactional
    public void delete(UUID id) {
        annotationRepository.deleteById(id);
    }

    /**
     * Delete all annotations for a study.
     */
    @Transactional
    public void deleteByStudy(String studyInstanceUid) {
        annotationRepository.deleteByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Delete all annotations for a series.
     */
    @Transactional
    public void deleteBySeries(String seriesInstanceUid) {
        annotationRepository.deleteBySeriesInstanceUid(seriesInstanceUid);
    }

    /**
     * Delete all annotations for an instance.
     */
    @Transactional
    public void deleteByInstance(String sopInstanceUid) {
        annotationRepository.deleteBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Toggle visibility of an annotation.
     */
    @Transactional
    public Annotation toggleVisibility(UUID id) {
        Optional<Annotation> existing = annotationRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Annotation not found: " + id);
        }

        Annotation annotation = existing.get();
        annotation.setVisible(!annotation.isVisible());
        return annotationRepository.save(annotation);
    }

    /**
     * Toggle lock state of an annotation.
     */
    @Transactional
    public Annotation toggleLock(UUID id) {
        Optional<Annotation> existing = annotationRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Annotation not found: " + id);
        }

        Annotation annotation = existing.get();
        annotation.setLocked(!annotation.isLocked());
        return annotationRepository.save(annotation);
    }

    /**
     * Count annotations for a study.
     */
    public long countByStudy(String studyInstanceUid) {
        return annotationRepository.countByStudyInstanceUid(studyInstanceUid);
    }
}
