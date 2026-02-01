package com.dicomviewer.service;

import com.dicomviewer.model.entity.Annotation;
import com.dicomviewer.repository.AnnotationRepository;
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
 * Unit tests for AnnotationService.
 */
@ExtendWith(MockitoExtension.class)
class AnnotationServiceTest {

    @Mock
    private AnnotationRepository annotationRepository;

    @InjectMocks
    private AnnotationService annotationService;

    private Annotation testAnnotation;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testAnnotation = new Annotation();
        testAnnotation.setId(testId);
        testAnnotation.setStudyInstanceUid("1.2.3.4.5");
        testAnnotation.setSeriesInstanceUid("1.2.3.4.5.6");
        testAnnotation.setSopInstanceUid("1.2.3.4.5.6.7");
        testAnnotation.setAnnotationType(Annotation.AnnotationType.ARROW);
        testAnnotation.setToolName("ArrowAnnotate");
        testAnnotation.setText("Important finding");
        testAnnotation.setPointsJson("[{\"x\":100,\"y\":100},{\"x\":200,\"y\":150}]");
        testAnnotation.setColor("#FF0000");
        testAnnotation.setVisible(true);
        testAnnotation.setLocked(false);
    }

    @Test
    void testCreate() {
        when(annotationRepository.save(any(Annotation.class))).thenReturn(testAnnotation);

        Annotation result = annotationService.create(testAnnotation);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("1.2.3.4.5", result.getStudyInstanceUid());
        assertEquals(Annotation.AnnotationType.ARROW, result.getAnnotationType());
        verify(annotationRepository, times(1)).save(testAnnotation);
    }

    @Test
    void testUpdate() {
        Annotation updateData = new Annotation();
        updateData.setText("Updated text");
        updateData.setColor("#00FF00");
        updateData.setFontSize(14);
        updateData.setVisible(false);
        updateData.setLocked(true);

        when(annotationRepository.findById(testId)).thenReturn(Optional.of(testAnnotation));
        when(annotationRepository.save(any(Annotation.class))).thenReturn(testAnnotation);

        Annotation result = annotationService.update(testId, updateData);

        assertNotNull(result);
        assertEquals("Updated text", result.getText());
        assertEquals("#00FF00", result.getColor());
        assertEquals(14, result.getFontSize());
        assertFalse(result.isVisible());
        assertTrue(result.isLocked());
        verify(annotationRepository, times(1)).save(any(Annotation.class));
    }

    @Test
    void testUpdateNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            annotationService.update(nonExistentId, testAnnotation);
        });
    }

    @Test
    void testGetById() {
        when(annotationRepository.findById(testId)).thenReturn(Optional.of(testAnnotation));

        Optional<Annotation> result = annotationService.getById(testId);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
    }

    @Test
    void testGetByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Annotation> result = annotationService.getById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetByStudy() {
        String studyUid = "1.2.3.4.5";
        when(annotationRepository.findByStudyInstanceUid(studyUid))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getByStudy(studyUid);

        assertEquals(1, results.size());
        assertEquals(studyUid, results.get(0).getStudyInstanceUid());
    }

    @Test
    void testGetBySeries() {
        String seriesUid = "1.2.3.4.5.6";
        when(annotationRepository.findBySeriesInstanceUid(seriesUid))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getBySeries(seriesUid);

        assertEquals(1, results.size());
        assertEquals(seriesUid, results.get(0).getSeriesInstanceUid());
    }

    @Test
    void testGetByInstance() {
        String sopUid = "1.2.3.4.5.6.7";
        when(annotationRepository.findBySopInstanceUid(sopUid))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getByInstance(sopUid);

        assertEquals(1, results.size());
        assertEquals(sopUid, results.get(0).getSopInstanceUid());
    }

    @Test
    void testGetByInstanceAndFrame() {
        String sopUid = "1.2.3.4.5.6.7";
        Integer frameIndex = 0;
        when(annotationRepository.findBySopInstanceUidAndFrameIndex(sopUid, frameIndex))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getByInstanceAndFrame(sopUid, frameIndex);

        assertEquals(1, results.size());
    }

    @Test
    void testGetVisibleByStudy() {
        String studyUid = "1.2.3.4.5";
        when(annotationRepository.findByStudyInstanceUidAndVisible(studyUid, true))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getVisibleByStudy(studyUid);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isVisible());
    }

    @Test
    void testGetByType() {
        when(annotationRepository.findByAnnotationType(Annotation.AnnotationType.ARROW))
                .thenReturn(List.of(testAnnotation));

        List<Annotation> results = annotationService.getByType(Annotation.AnnotationType.ARROW);

        assertEquals(1, results.size());
        assertEquals(Annotation.AnnotationType.ARROW, results.get(0).getAnnotationType());
    }

    @Test
    void testDelete() {
        doNothing().when(annotationRepository).deleteById(testId);

        annotationService.delete(testId);

        verify(annotationRepository, times(1)).deleteById(testId);
    }

    @Test
    void testDeleteByStudy() {
        String studyUid = "1.2.3.4.5";
        doNothing().when(annotationRepository).deleteByStudyInstanceUid(studyUid);

        annotationService.deleteByStudy(studyUid);

        verify(annotationRepository, times(1)).deleteByStudyInstanceUid(studyUid);
    }

    @Test
    void testDeleteBySeries() {
        String seriesUid = "1.2.3.4.5.6";
        doNothing().when(annotationRepository).deleteBySeriesInstanceUid(seriesUid);

        annotationService.deleteBySeries(seriesUid);

        verify(annotationRepository, times(1)).deleteBySeriesInstanceUid(seriesUid);
    }

    @Test
    void testDeleteByInstance() {
        String sopUid = "1.2.3.4.5.6.7";
        doNothing().when(annotationRepository).deleteBySopInstanceUid(sopUid);

        annotationService.deleteByInstance(sopUid);

        verify(annotationRepository, times(1)).deleteBySopInstanceUid(sopUid);
    }

    @Test
    void testToggleVisibility() {
        testAnnotation.setVisible(true);
        when(annotationRepository.findById(testId)).thenReturn(Optional.of(testAnnotation));
        when(annotationRepository.save(any(Annotation.class))).thenAnswer(invocation -> {
            Annotation a = invocation.getArgument(0);
            return a;
        });

        Annotation result = annotationService.toggleVisibility(testId);

        assertFalse(result.isVisible());
    }

    @Test
    void testToggleVisibilityNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            annotationService.toggleVisibility(nonExistentId);
        });
    }

    @Test
    void testToggleLock() {
        testAnnotation.setLocked(false);
        when(annotationRepository.findById(testId)).thenReturn(Optional.of(testAnnotation));
        when(annotationRepository.save(any(Annotation.class))).thenAnswer(invocation -> {
            Annotation a = invocation.getArgument(0);
            return a;
        });

        Annotation result = annotationService.toggleLock(testId);

        assertTrue(result.isLocked());
    }

    @Test
    void testToggleLockNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            annotationService.toggleLock(nonExistentId);
        });
    }

    @Test
    void testCountByStudy() {
        String studyUid = "1.2.3.4.5";
        when(annotationRepository.countByStudyInstanceUid(studyUid)).thenReturn(3L);

        long count = annotationService.countByStudy(studyUid);

        assertEquals(3L, count);
    }
}
