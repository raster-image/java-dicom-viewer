package com.dicomviewer.service;

import com.dicomviewer.model.entity.KeyImage;
import com.dicomviewer.repository.KeyImageRepository;
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
 * Unit tests for KeyImageService.
 */
@ExtendWith(MockitoExtension.class)
class KeyImageServiceTest {

    @Mock
    private KeyImageRepository keyImageRepository;

    @InjectMocks
    private KeyImageService keyImageService;

    private KeyImage testKeyImage;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testKeyImage = new KeyImage();
        testKeyImage.setId(testId);
        testKeyImage.setStudyInstanceUid("1.2.3.4.5");
        testKeyImage.setSeriesInstanceUid("1.2.3.4.5.6");
        testKeyImage.setSopInstanceUid("1.2.3.4.5.6.7");
        testKeyImage.setFrameIndex(0);
        testKeyImage.setInstanceNumber(1);
        testKeyImage.setDescription("Important finding");
        testKeyImage.setCategory("findings");
        testKeyImage.setWindowWidth(400.0);
        testKeyImage.setWindowCenter(40.0);
    }

    @Test
    void testToggleKeyImage_CreateNew() {
        KeyImage newKeyImage = new KeyImage();
        newKeyImage.setSopInstanceUid("1.2.3.4.5.6.7");
        newKeyImage.setFrameIndex(0);
        newKeyImage.setStudyInstanceUid("1.2.3.4.5");
        newKeyImage.setSeriesInstanceUid("1.2.3.4.5.6");

        when(keyImageRepository.findBySopInstanceUidAndFrameIndex("1.2.3.4.5.6.7", 0))
                .thenReturn(Optional.empty());
        when(keyImageRepository.save(any(KeyImage.class))).thenReturn(testKeyImage);

        KeyImage result = keyImageService.toggleKeyImage(newKeyImage);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(keyImageRepository, times(1)).save(any(KeyImage.class));
        verify(keyImageRepository, never()).delete(any(KeyImage.class));
    }

    @Test
    void testToggleKeyImage_RemoveExisting() {
        KeyImage existingKeyImage = new KeyImage();
        existingKeyImage.setId(testId);
        existingKeyImage.setSopInstanceUid("1.2.3.4.5.6.7");
        existingKeyImage.setFrameIndex(0);

        when(keyImageRepository.findBySopInstanceUidAndFrameIndex("1.2.3.4.5.6.7", 0))
                .thenReturn(Optional.of(testKeyImage));
        doNothing().when(keyImageRepository).delete(testKeyImage);

        KeyImage result = keyImageService.toggleKeyImage(existingKeyImage);

        assertNull(result);
        verify(keyImageRepository, times(1)).delete(testKeyImage);
        verify(keyImageRepository, never()).save(any(KeyImage.class));
    }

    @Test
    void testToggleKeyImage_WithNullFrameIndex() {
        KeyImage newKeyImage = new KeyImage();
        newKeyImage.setSopInstanceUid("1.2.3.4.5.6.7");
        newKeyImage.setFrameIndex(null);
        newKeyImage.setStudyInstanceUid("1.2.3.4.5");
        newKeyImage.setSeriesInstanceUid("1.2.3.4.5.6");

        when(keyImageRepository.findBySopInstanceUidAndFrameIndex("1.2.3.4.5.6.7", 0))
                .thenReturn(Optional.empty());
        when(keyImageRepository.save(any(KeyImage.class))).thenAnswer(invocation -> {
            KeyImage saved = invocation.getArgument(0);
            assertEquals(0, saved.getFrameIndex()); // Frame index should default to 0
            return testKeyImage;
        });

        KeyImage result = keyImageService.toggleKeyImage(newKeyImage);

        assertNotNull(result);
    }

    @Test
    void testCreate() {
        when(keyImageRepository.existsBySopInstanceUidAndFrameIndex("1.2.3.4.5.6.7", 0))
                .thenReturn(false);
        when(keyImageRepository.save(any(KeyImage.class))).thenReturn(testKeyImage);

        KeyImage result = keyImageService.create(testKeyImage);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(keyImageRepository, times(1)).save(testKeyImage);
    }

    @Test
    void testCreate_AlreadyExists() {
        when(keyImageRepository.existsBySopInstanceUidAndFrameIndex("1.2.3.4.5.6.7", 0))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            keyImageService.create(testKeyImage);
        });

        verify(keyImageRepository, never()).save(any(KeyImage.class));
    }

    @Test
    void testUpdate() {
        KeyImage updateData = new KeyImage();
        updateData.setDescription("Updated description");
        updateData.setCategory("critical");
        updateData.setWindowWidth(500.0);
        updateData.setWindowCenter(50.0);

        when(keyImageRepository.findById(testId)).thenReturn(Optional.of(testKeyImage));
        when(keyImageRepository.save(any(KeyImage.class))).thenReturn(testKeyImage);

        KeyImage result = keyImageService.update(testId, updateData);

        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        assertEquals("critical", result.getCategory());
        assertEquals(500.0, result.getWindowWidth());
        assertEquals(50.0, result.getWindowCenter());
        verify(keyImageRepository, times(1)).save(any(KeyImage.class));
    }

    @Test
    void testUpdateNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(keyImageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            keyImageService.update(nonExistentId, testKeyImage);
        });
    }

    @Test
    void testGetById() {
        when(keyImageRepository.findById(testId)).thenReturn(Optional.of(testKeyImage));

        Optional<KeyImage> result = keyImageService.getById(testId);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
    }

    @Test
    void testGetByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(keyImageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<KeyImage> result = keyImageService.getById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetByStudy() {
        String studyUid = "1.2.3.4.5";
        when(keyImageRepository.findByStudyInstanceUid(studyUid))
                .thenReturn(List.of(testKeyImage));

        List<KeyImage> results = keyImageService.getByStudy(studyUid);

        assertEquals(1, results.size());
        assertEquals(studyUid, results.get(0).getStudyInstanceUid());
    }

    @Test
    void testGetBySeries() {
        String seriesUid = "1.2.3.4.5.6";
        when(keyImageRepository.findBySeriesInstanceUid(seriesUid))
                .thenReturn(List.of(testKeyImage));

        List<KeyImage> results = keyImageService.getBySeries(seriesUid);

        assertEquals(1, results.size());
        assertEquals(seriesUid, results.get(0).getSeriesInstanceUid());
    }

    @Test
    void testGetByInstance() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.findBySopInstanceUid(sopUid))
                .thenReturn(List.of(testKeyImage));

        List<KeyImage> results = keyImageService.getByInstance(sopUid);

        assertEquals(1, results.size());
        assertEquals(sopUid, results.get(0).getSopInstanceUid());
    }

    @Test
    void testIsKeyImage_True() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.existsBySopInstanceUidAndFrameIndex(sopUid, 0))
                .thenReturn(true);

        boolean result = keyImageService.isKeyImage(sopUid, 0);

        assertTrue(result);
    }

    @Test
    void testIsKeyImage_False() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.existsBySopInstanceUidAndFrameIndex(sopUid, 0))
                .thenReturn(false);

        boolean result = keyImageService.isKeyImage(sopUid, 0);

        assertFalse(result);
    }

    @Test
    void testIsKeyImage_NullFrameIndex() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.existsBySopInstanceUidAndFrameIndex(sopUid, 0))
                .thenReturn(true);

        boolean result = keyImageService.isKeyImage(sopUid, null);

        assertTrue(result);
        // Verify it was called with 0 as default
        verify(keyImageRepository).existsBySopInstanceUidAndFrameIndex(sopUid, 0);
    }

    @Test
    void testGetByCategory() {
        String category = "findings";
        when(keyImageRepository.findByCategory(category))
                .thenReturn(List.of(testKeyImage));

        List<KeyImage> results = keyImageService.getByCategory(category);

        assertEquals(1, results.size());
        assertEquals(category, results.get(0).getCategory());
    }

    @Test
    void testGetByStudyAndCategory() {
        String studyUid = "1.2.3.4.5";
        String category = "findings";
        when(keyImageRepository.findByStudyInstanceUidAndCategory(studyUid, category))
                .thenReturn(List.of(testKeyImage));

        List<KeyImage> results = keyImageService.getByStudyAndCategory(studyUid, category);

        assertEquals(1, results.size());
        assertEquals(studyUid, results.get(0).getStudyInstanceUid());
        assertEquals(category, results.get(0).getCategory());
    }

    @Test
    void testDelete() {
        doNothing().when(keyImageRepository).deleteById(testId);

        keyImageService.delete(testId);

        verify(keyImageRepository, times(1)).deleteById(testId);
    }

    @Test
    void testDeleteByInstanceAndFrame() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.findBySopInstanceUidAndFrameIndex(sopUid, 0))
                .thenReturn(Optional.of(testKeyImage));
        doNothing().when(keyImageRepository).delete(testKeyImage);

        keyImageService.deleteByInstanceAndFrame(sopUid, 0);

        verify(keyImageRepository, times(1)).delete(testKeyImage);
    }

    @Test
    void testDeleteByInstanceAndFrame_NotFound() {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageRepository.findBySopInstanceUidAndFrameIndex(sopUid, 0))
                .thenReturn(Optional.empty());

        keyImageService.deleteByInstanceAndFrame(sopUid, 0);

        verify(keyImageRepository, never()).delete(any(KeyImage.class));
    }

    @Test
    void testDeleteByStudy() {
        String studyUid = "1.2.3.4.5";
        doNothing().when(keyImageRepository).deleteByStudyInstanceUid(studyUid);

        keyImageService.deleteByStudy(studyUid);

        verify(keyImageRepository, times(1)).deleteByStudyInstanceUid(studyUid);
    }

    @Test
    void testCountByStudy() {
        String studyUid = "1.2.3.4.5";
        when(keyImageRepository.countByStudyInstanceUid(studyUid)).thenReturn(5L);

        long count = keyImageService.countByStudy(studyUid);

        assertEquals(5L, count);
    }
}
