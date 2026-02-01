package com.dicomviewer.service;

import com.dicomviewer.model.entity.KeyImage;
import com.dicomviewer.repository.KeyImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing key images.
 */
@Service
public class KeyImageService {

    private final KeyImageRepository keyImageRepository;

    @Autowired
    public KeyImageService(KeyImageRepository keyImageRepository) {
        this.keyImageRepository = keyImageRepository;
    }

    /**
     * Create or toggle a key image.
     * If the key image already exists, it will be removed (toggle off).
     * If it doesn't exist, it will be created (toggle on).
     */
    @Transactional
    public KeyImage toggleKeyImage(KeyImage keyImage) {
        Integer frameIndex = keyImage.getFrameIndex() != null ? keyImage.getFrameIndex() : 0;
        Optional<KeyImage> existing = keyImageRepository.findBySopInstanceUidAndFrameIndex(
                keyImage.getSopInstanceUid(), frameIndex);

        if (existing.isPresent()) {
            keyImageRepository.delete(existing.get());
            return null; // Key image removed
        }

        keyImage.setFrameIndex(frameIndex);
        return keyImageRepository.save(keyImage);
    }

    /**
     * Create a new key image.
     */
    @Transactional
    public KeyImage create(KeyImage keyImage) {
        Integer frameIndex = keyImage.getFrameIndex() != null ? keyImage.getFrameIndex() : 0;
        
        if (keyImageRepository.existsBySopInstanceUidAndFrameIndex(
                keyImage.getSopInstanceUid(), frameIndex)) {
            throw new IllegalArgumentException("Key image already exists for this instance and frame");
        }

        keyImage.setFrameIndex(frameIndex);
        return keyImageRepository.save(keyImage);
    }

    /**
     * Update an existing key image.
     */
    @Transactional
    public KeyImage update(UUID id, KeyImage keyImage) {
        Optional<KeyImage> existing = keyImageRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Key image not found: " + id);
        }

        KeyImage updated = existing.get();
        if (keyImage.getDescription() != null) {
            updated.setDescription(keyImage.getDescription());
        }
        if (keyImage.getCategory() != null) {
            updated.setCategory(keyImage.getCategory());
        }
        if (keyImage.getWindowWidth() != null) {
            updated.setWindowWidth(keyImage.getWindowWidth());
        }
        if (keyImage.getWindowCenter() != null) {
            updated.setWindowCenter(keyImage.getWindowCenter());
        }

        return keyImageRepository.save(updated);
    }

    /**
     * Get a key image by ID.
     */
    public Optional<KeyImage> getById(UUID id) {
        return keyImageRepository.findById(id);
    }

    /**
     * Get all key images for a study.
     */
    public List<KeyImage> getByStudy(String studyInstanceUid) {
        return keyImageRepository.findByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Get all key images for a series.
     */
    public List<KeyImage> getBySeries(String seriesInstanceUid) {
        return keyImageRepository.findBySeriesInstanceUid(seriesInstanceUid);
    }

    /**
     * Get all key images for an instance.
     */
    public List<KeyImage> getByInstance(String sopInstanceUid) {
        return keyImageRepository.findBySopInstanceUid(sopInstanceUid);
    }

    /**
     * Check if an image is marked as key image.
     */
    public boolean isKeyImage(String sopInstanceUid, Integer frameIndex) {
        return keyImageRepository.existsBySopInstanceUidAndFrameIndex(
                sopInstanceUid, frameIndex != null ? frameIndex : 0);
    }

    /**
     * Get key images by category.
     */
    public List<KeyImage> getByCategory(String category) {
        return keyImageRepository.findByCategory(category);
    }

    /**
     * Get key images by study and category.
     */
    public List<KeyImage> getByStudyAndCategory(String studyInstanceUid, String category) {
        return keyImageRepository.findByStudyInstanceUidAndCategory(studyInstanceUid, category);
    }

    /**
     * Delete a key image.
     */
    @Transactional
    public void delete(UUID id) {
        keyImageRepository.deleteById(id);
    }

    /**
     * Delete a key image by instance and frame.
     */
    @Transactional
    public void deleteByInstanceAndFrame(String sopInstanceUid, Integer frameIndex) {
        Optional<KeyImage> existing = keyImageRepository.findBySopInstanceUidAndFrameIndex(
                sopInstanceUid, frameIndex != null ? frameIndex : 0);
        existing.ifPresent(keyImageRepository::delete);
    }

    /**
     * Delete all key images for a study.
     */
    @Transactional
    public void deleteByStudy(String studyInstanceUid) {
        keyImageRepository.deleteByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * Count key images for a study.
     */
    public long countByStudy(String studyInstanceUid) {
        return keyImageRepository.countByStudyInstanceUid(studyInstanceUid);
    }
}
