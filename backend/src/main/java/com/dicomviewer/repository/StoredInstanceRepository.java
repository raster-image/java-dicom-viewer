package com.dicomviewer.repository;

import com.dicomviewer.model.entity.StoredInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoredInstanceRepository extends JpaRepository<StoredInstance, Long> {

    Optional<StoredInstance> findBySopInstanceUid(String sopInstanceUid);

    boolean existsBySopInstanceUid(String sopInstanceUid);

    List<StoredInstance> findByStudyInstanceUid(String studyInstanceUid);

    List<StoredInstance> findBySeriesInstanceUid(String seriesInstanceUid);

    List<StoredInstance> findByPatientId(String patientId);

    @Query("SELECT COALESCE(SUM(s.fileSize), 0) FROM StoredInstance s")
    long sumFileSize();

    @Query("SELECT COUNT(DISTINCT s.studyInstanceUid) FROM StoredInstance s")
    long countDistinctStudies();

    @Query("SELECT COUNT(DISTINCT s.seriesInstanceUid) FROM StoredInstance s")
    long countDistinctSeries();

    void deleteByStudyInstanceUid(String studyInstanceUid);

    void deleteBySeriesInstanceUid(String seriesInstanceUid);
}
