package com.dicomviewer.repository;

import com.dicomviewer.model.StudyCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for cached DICOM studies.
 */
@Repository
public interface StudyCacheRepository extends JpaRepository<StudyCache, String> {

    List<StudyCache> findByPatientId(String patientId);

    List<StudyCache> findByPatientNameContainingIgnoreCase(String patientName);

    List<StudyCache> findByStudyDateBetween(LocalDate startDate, LocalDate endDate);

    List<StudyCache> findByModality(String modality);

    @Query("SELECT s FROM StudyCache s WHERE " +
           "(:patientId IS NULL OR s.patientId = :patientId) AND " +
           "(:patientName IS NULL OR LOWER(s.patientName) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND " +
           "(:studyDate IS NULL OR s.studyDate = :studyDate) AND " +
           "(:modality IS NULL OR s.modality = :modality) AND " +
           "(:accessionNumber IS NULL OR s.accessionNumber = :accessionNumber)")
    List<StudyCache> search(@Param("patientId") String patientId,
                            @Param("patientName") String patientName,
                            @Param("studyDate") LocalDate studyDate,
                            @Param("modality") String modality,
                            @Param("accessionNumber") String accessionNumber);
}
