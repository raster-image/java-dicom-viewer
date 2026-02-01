package com.dicomviewer.repository;

import com.dicomviewer.model.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    List<Measurement> findByStudyInstanceUid(String studyInstanceUid);

    List<Measurement> findBySeriesInstanceUid(String seriesInstanceUid);

    List<Measurement> findBySopInstanceUid(String sopInstanceUid);

    List<Measurement> findByStudyInstanceUidAndVisible(String studyInstanceUid, boolean visible);

    List<Measurement> findBySopInstanceUidAndFrameIndex(String sopInstanceUid, Integer frameIndex);

    List<Measurement> findByCreatedBy(String createdBy);

    List<Measurement> findByMeasurementType(Measurement.MeasurementType measurementType);

    List<Measurement> findByStudyInstanceUidAndMeasurementType(
            String studyInstanceUid, Measurement.MeasurementType measurementType);

    void deleteByStudyInstanceUid(String studyInstanceUid);

    void deleteBySeriesInstanceUid(String seriesInstanceUid);

    void deleteBySopInstanceUid(String sopInstanceUid);

    long countByStudyInstanceUid(String studyInstanceUid);

    long countBySopInstanceUid(String sopInstanceUid);
}
