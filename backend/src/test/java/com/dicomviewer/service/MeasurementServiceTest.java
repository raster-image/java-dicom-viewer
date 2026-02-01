package com.dicomviewer.service;

import com.dicomviewer.model.entity.Measurement;
import com.dicomviewer.repository.MeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MeasurementService.
 */
@ExtendWith(MockitoExtension.class)
class MeasurementServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private MeasurementService measurementService;

    private Measurement testMeasurement;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testMeasurement = new Measurement();
        testMeasurement.setId(testId);
        testMeasurement.setStudyInstanceUid("1.2.3.4.5");
        testMeasurement.setSeriesInstanceUid("1.2.3.4.5.6");
        testMeasurement.setSopInstanceUid("1.2.3.4.5.6.7");
        testMeasurement.setMeasurementType(Measurement.MeasurementType.LENGTH);
        testMeasurement.setToolName("Length");
        testMeasurement.setValue(45.5);
        testMeasurement.setUnit("mm");
        testMeasurement.setPointsJson("[{\"x\":100,\"y\":100},{\"x\":200,\"y\":200}]");
        testMeasurement.setVisible(true);
    }

    @Test
    void testCreate() {
        when(measurementRepository.save(any(Measurement.class))).thenReturn(testMeasurement);

        Measurement result = measurementService.create(testMeasurement);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("1.2.3.4.5", result.getStudyInstanceUid());
        assertEquals(Measurement.MeasurementType.LENGTH, result.getMeasurementType());
        verify(measurementRepository, times(1)).save(testMeasurement);
    }

    @Test
    void testUpdate() {
        Measurement updateData = new Measurement();
        updateData.setLabel("Updated Label");
        updateData.setValue(50.0);
        updateData.setColor("#FF0000");
        updateData.setVisible(false);

        when(measurementRepository.findById(testId)).thenReturn(Optional.of(testMeasurement));
        when(measurementRepository.save(any(Measurement.class))).thenReturn(testMeasurement);

        Measurement result = measurementService.update(testId, updateData);

        assertNotNull(result);
        assertEquals("Updated Label", result.getLabel());
        assertEquals(50.0, result.getValue());
        assertEquals("#FF0000", result.getColor());
        assertFalse(result.isVisible());
        verify(measurementRepository, times(1)).save(any(Measurement.class));
    }

    @Test
    void testUpdateNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            measurementService.update(nonExistentId, testMeasurement);
        });
    }

    @Test
    void testGetById() {
        when(measurementRepository.findById(testId)).thenReturn(Optional.of(testMeasurement));

        Optional<Measurement> result = measurementService.getById(testId);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
    }

    @Test
    void testGetByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Measurement> result = measurementService.getById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetByStudy() {
        String studyUid = "1.2.3.4.5";
        when(measurementRepository.findByStudyInstanceUid(studyUid))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getByStudy(studyUid);

        assertEquals(1, results.size());
        assertEquals(studyUid, results.get(0).getStudyInstanceUid());
    }

    @Test
    void testGetBySeries() {
        String seriesUid = "1.2.3.4.5.6";
        when(measurementRepository.findBySeriesInstanceUid(seriesUid))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getBySeries(seriesUid);

        assertEquals(1, results.size());
        assertEquals(seriesUid, results.get(0).getSeriesInstanceUid());
    }

    @Test
    void testGetByInstance() {
        String sopUid = "1.2.3.4.5.6.7";
        when(measurementRepository.findBySopInstanceUid(sopUid))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getByInstance(sopUid);

        assertEquals(1, results.size());
        assertEquals(sopUid, results.get(0).getSopInstanceUid());
    }

    @Test
    void testGetByInstanceAndFrame() {
        String sopUid = "1.2.3.4.5.6.7";
        Integer frameIndex = 0;
        when(measurementRepository.findBySopInstanceUidAndFrameIndex(sopUid, frameIndex))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getByInstanceAndFrame(sopUid, frameIndex);

        assertEquals(1, results.size());
    }

    @Test
    void testGetVisibleByStudy() {
        String studyUid = "1.2.3.4.5";
        when(measurementRepository.findByStudyInstanceUidAndVisible(studyUid, true))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getVisibleByStudy(studyUid);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isVisible());
    }

    @Test
    void testGetByType() {
        when(measurementRepository.findByMeasurementType(Measurement.MeasurementType.LENGTH))
                .thenReturn(List.of(testMeasurement));

        List<Measurement> results = measurementService.getByType(Measurement.MeasurementType.LENGTH);

        assertEquals(1, results.size());
        assertEquals(Measurement.MeasurementType.LENGTH, results.get(0).getMeasurementType());
    }

    @Test
    void testDelete() {
        doNothing().when(measurementRepository).deleteById(testId);

        measurementService.delete(testId);

        verify(measurementRepository, times(1)).deleteById(testId);
    }

    @Test
    void testDeleteByStudy() {
        String studyUid = "1.2.3.4.5";
        doNothing().when(measurementRepository).deleteByStudyInstanceUid(studyUid);

        measurementService.deleteByStudy(studyUid);

        verify(measurementRepository, times(1)).deleteByStudyInstanceUid(studyUid);
    }

    @Test
    void testDeleteBySeries() {
        String seriesUid = "1.2.3.4.5.6";
        doNothing().when(measurementRepository).deleteBySeriesInstanceUid(seriesUid);

        measurementService.deleteBySeries(seriesUid);

        verify(measurementRepository, times(1)).deleteBySeriesInstanceUid(seriesUid);
    }

    @Test
    void testDeleteByInstance() {
        String sopUid = "1.2.3.4.5.6.7";
        doNothing().when(measurementRepository).deleteBySopInstanceUid(sopUid);

        measurementService.deleteByInstance(sopUid);

        verify(measurementRepository, times(1)).deleteBySopInstanceUid(sopUid);
    }

    @Test
    void testToggleVisibility() {
        testMeasurement.setVisible(true);
        when(measurementRepository.findById(testId)).thenReturn(Optional.of(testMeasurement));
        when(measurementRepository.save(any(Measurement.class))).thenAnswer(invocation -> {
            Measurement m = invocation.getArgument(0);
            return m;
        });

        Measurement result = measurementService.toggleVisibility(testId);

        assertFalse(result.isVisible());
    }

    @Test
    void testToggleVisibilityNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            measurementService.toggleVisibility(nonExistentId);
        });
    }

    @Test
    void testCountByStudy() {
        String studyUid = "1.2.3.4.5";
        when(measurementRepository.countByStudyInstanceUid(studyUid)).thenReturn(5L);

        long count = measurementService.countByStudy(studyUid);

        assertEquals(5L, count);
    }
}
