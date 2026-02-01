package com.dicomviewer.service;

import com.dicomviewer.model.entity.Measurement;
import com.dicomviewer.repository.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing DICOM image measurements.
 */
@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;

    @Autowired
    public MeasurementService(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    /**
     * Create a new measurement.
     */
    @Transactional
    public Measurement create(Measurement measurement) {
        return measurementRepository.save(measurement);
    }

    /**
     * Update an existing measurement.
     */
    @Transactional
    public Measurement update(UUID id, Measurement measurement) {
        Optional<Measurement> existing = measurementRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Measurement not found: " + id);
        }

        Measurement updated = existing.get();
        if (measurement.getLabel() != null) {
            updated.setLabel(measurement.getLabel());
        }
        if (measurement.getValue() != null) {
            updated.setValue(measurement.getValue());
        }
        if (measurement.getPointsJson() != null) {
            updated.setPointsJson(measurement.getPointsJson());
        }
        if (measurement.getRoiStatsJson() != null) {
            updated.setRoiStatsJson(measurement.getRoiStatsJson());
        }
        if (measurement.getColor() != null) {
            updated.setColor(measurement.getColor());
        }
        updated.setVisible(measurement.isVisible());

        return measurementRepository.save(updated);
    }

    /**
     * Get a measurement by ID.
     */
    public Optional<Measurement> getById(UUID id) {
        return measurementRepository.findById(id);
    }

    /**
     * Get all measurements for a study.
     */
    public List<Measurement> getByStudy(String studyInstanceUid) {
        return measurementRepository.findByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Get all measurements for a series.
     */
    public List<Measurement> getBySeries(String seriesInstanceUid) {
        return measurementRepository.findBySeriesInstanceUid(seriesInstanceUid);
    }

    /**
     * Get all measurements for an instance.
     */
    public List<Measurement> getByInstance(String sopInstanceUid) {
        return measurementRepository.findBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Get measurements for a specific frame.
     */
    public List<Measurement> getByInstanceAndFrame(String sopInstanceUid, Integer frameIndex) {
        return measurementRepository.findBySopInstanceUidAndFrameIndex(sopInstanceUid, frameIndex);
    }

    /**
     * Get visible measurements for a study.
     */
    public List<Measurement> getVisibleByStudy(String studyInstanceUid) {
        return measurementRepository.findByStudyInstanceUidAndVisible(studyInstanceUid, true);
    }

    /**
     * Get measurements by type.
     */
    public List<Measurement> getByType(Measurement.MeasurementType type) {
        return measurementRepository.findByMeasurementType(type);
    }

    /**
     * Delete a measurement.
     */
    @Transactional
    public void delete(UUID id) {
        measurementRepository.deleteById(id);
    }

    /**
     * Delete all measurements for a study.
     */
    @Transactional
    public void deleteByStudy(String studyInstanceUid) {
        measurementRepository.deleteByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Delete all measurements for a series.
     */
    @Transactional
    public void deleteBySeries(String seriesInstanceUid) {
        measurementRepository.deleteBySeriesInstanceUid(seriesInstanceUid);
    }

    /**
     * Delete all measurements for an instance.
     */
    @Transactional
    public void deleteByInstance(String sopInstanceUid) {
        measurementRepository.deleteBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Toggle visibility of a measurement.
     */
    @Transactional
    public Measurement toggleVisibility(UUID id) {
        Optional<Measurement> existing = measurementRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Measurement not found: " + id);
        }

        Measurement measurement = existing.get();
        measurement.setVisible(!measurement.isVisible());
        return measurementRepository.save(measurement);
    }

    /**
     * Count measurements for a study.
     */
    public long countByStudy(String studyInstanceUid) {
        return measurementRepository.countByStudyInstanceUid(studyInstanceUid);
    }
}
