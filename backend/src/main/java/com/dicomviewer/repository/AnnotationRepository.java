package com.dicomviewer.repository;

import com.dicomviewer.model.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, UUID> {

    List<Annotation> findByStudyInstanceUid(String studyInstanceUid);

    List<Annotation> findBySeriesInstanceUid(String seriesInstanceUid);

    List<Annotation> findBySopInstanceUid(String sopInstanceUid);

    List<Annotation> findByStudyInstanceUidAndVisible(String studyInstanceUid, boolean visible);

    List<Annotation> findBySopInstanceUidAndFrameIndex(String sopInstanceUid, Integer frameIndex);

    List<Annotation> findByCreatedBy(String createdBy);

    List<Annotation> findByAnnotationType(Annotation.AnnotationType annotationType);

    List<Annotation> findByStudyInstanceUidAndAnnotationType(
            String studyInstanceUid, Annotation.AnnotationType annotationType);

    void deleteByStudyInstanceUid(String studyInstanceUid);

    void deleteBySeriesInstanceUid(String seriesInstanceUid);

    void deleteBySopInstanceUid(String sopInstanceUid);

    long countByStudyInstanceUid(String studyInstanceUid);

    long countBySopInstanceUid(String sopInstanceUid);
}
