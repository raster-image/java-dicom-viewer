package com.dicomviewer.repository;

import com.dicomviewer.model.entity.KeyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KeyImageRepository extends JpaRepository<KeyImage, UUID> {

    List<KeyImage> findByStudyInstanceUid(String studyInstanceUid);

    List<KeyImage> findBySeriesInstanceUid(String seriesInstanceUid);

    List<KeyImage> findBySopInstanceUid(String sopInstanceUid);

    Optional<KeyImage> findBySopInstanceUidAndFrameIndex(String sopInstanceUid, Integer frameIndex);

    List<KeyImage> findByCreatedBy(String createdBy);

    List<KeyImage> findByCategory(String category);

    List<KeyImage> findByStudyInstanceUidAndCategory(String studyInstanceUid, String category);

    boolean existsBySopInstanceUidAndFrameIndex(String sopInstanceUid, Integer frameIndex);

    void deleteByStudyInstanceUid(String studyInstanceUid);

    void deleteBySeriesInstanceUid(String seriesInstanceUid);

    void deleteBySopInstanceUid(String sopInstanceUid);

    long countByStudyInstanceUid(String studyInstanceUid);
}
