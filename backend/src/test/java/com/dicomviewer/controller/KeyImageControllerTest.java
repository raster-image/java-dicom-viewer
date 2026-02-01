package com.dicomviewer.controller;

import com.dicomviewer.model.entity.KeyImage;
import com.dicomviewer.service.KeyImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for KeyImageController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class KeyImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void testCreateKeyImage() throws Exception {
        when(keyImageService.create(any(KeyImage.class))).thenReturn(testKeyImage);

        mockMvc.perform(post("/api/key-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testKeyImage)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"))
                .andExpect(jsonPath("$.description").value("Important finding"));

        verify(keyImageService, times(1)).create(any(KeyImage.class));
    }

    @Test
    void testCreateKeyImageAlreadyExists() throws Exception {
        when(keyImageService.create(any(KeyImage.class)))
                .thenThrow(new IllegalArgumentException("Key image already exists for this instance and frame"));

        mockMvc.perform(post("/api/key-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testKeyImage)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ALREADY_EXISTS"));
    }

    @Test
    void testToggleKeyImage_Add() throws Exception {
        when(keyImageService.toggleKeyImage(any(KeyImage.class))).thenReturn(testKeyImage);

        mockMvc.perform(post("/api/key-images/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testKeyImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("added"))
                .andExpect(jsonPath("$.isKeyImage").value(true))
                .andExpect(jsonPath("$.keyImage.id").value(testId.toString()));
    }

    @Test
    void testToggleKeyImage_Remove() throws Exception {
        when(keyImageService.toggleKeyImage(any(KeyImage.class))).thenReturn(null);

        mockMvc.perform(post("/api/key-images/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testKeyImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("removed"))
                .andExpect(jsonPath("$.isKeyImage").value(false));
    }

    @Test
    void testGetKeyImageById() throws Exception {
        when(keyImageService.getById(testId)).thenReturn(Optional.of(testKeyImage));

        mockMvc.perform(get("/api/key-images/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"));
    }

    @Test
    void testGetKeyImageByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(keyImageService.getById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/key-images/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateKeyImage() throws Exception {
        KeyImage updateData = new KeyImage();
        updateData.setDescription("Updated description");
        updateData.setCategory("critical");

        when(keyImageService.update(eq(testId), any(KeyImage.class))).thenReturn(testKeyImage);

        mockMvc.perform(put("/api/key-images/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());

        verify(keyImageService, times(1)).update(eq(testId), any(KeyImage.class));
    }

    @Test
    void testUpdateKeyImageNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(keyImageService.update(eq(nonExistentId), any(KeyImage.class)))
                .thenThrow(new IllegalArgumentException("Key image not found: " + nonExistentId));

        mockMvc.perform(put("/api/key-images/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testKeyImage)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testDeleteKeyImage() throws Exception {
        doNothing().when(keyImageService).delete(testId);

        mockMvc.perform(delete("/api/key-images/{id}", testId))
                .andExpect(status().isNoContent());

        verify(keyImageService, times(1)).delete(testId);
    }

    @Test
    void testGetKeyImagesByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(keyImageService.getByStudy(studyUid)).thenReturn(List.of(testKeyImage));

        mockMvc.perform(get("/api/key-images/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.keyImages").isArray())
                .andExpect(jsonPath("$.keyImages[0].id").value(testId.toString()));
    }

    @Test
    void testGetKeyImagesByStudyWithCategory() throws Exception {
        String studyUid = "1.2.3.4.5";
        String category = "findings";
        when(keyImageService.getByStudyAndCategory(studyUid, category)).thenReturn(List.of(testKeyImage));

        mockMvc.perform(get("/api/key-images/study/{studyInstanceUid}", studyUid)
                .param("category", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        verify(keyImageService, times(1)).getByStudyAndCategory(studyUid, category);
    }

    @Test
    void testGetKeyImagesBySeries() throws Exception {
        String seriesUid = "1.2.3.4.5.6";
        when(keyImageService.getBySeries(seriesUid)).thenReturn(List.of(testKeyImage));

        mockMvc.perform(get("/api/key-images/series/{seriesInstanceUid}", seriesUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seriesInstanceUid").value(seriesUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetKeyImagesByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageService.getByInstance(sopUid)).thenReturn(List.of(testKeyImage));

        mockMvc.perform(get("/api/key-images/instance/{sopInstanceUid}", sopUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sopInstanceUid").value(sopUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testCheckKeyImage_True() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageService.isKeyImage(sopUid, 0)).thenReturn(true);

        mockMvc.perform(get("/api/key-images/check")
                .param("sopInstanceUid", sopUid)
                .param("frameIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sopInstanceUid").value(sopUid))
                .andExpect(jsonPath("$.frameIndex").value(0))
                .andExpect(jsonPath("$.isKeyImage").value(true));
    }

    @Test
    void testCheckKeyImage_False() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(keyImageService.isKeyImage(sopUid, 0)).thenReturn(false);

        mockMvc.perform(get("/api/key-images/check")
                .param("sopInstanceUid", sopUid)
                .param("frameIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isKeyImage").value(false));
    }

    @Test
    void testDeleteByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        doNothing().when(keyImageService).deleteByInstanceAndFrame(sopUid, 0);

        mockMvc.perform(delete("/api/key-images/instance/{sopInstanceUid}", sopUid)
                .param("frameIndex", "0"))
                .andExpect(status().isNoContent());

        verify(keyImageService, times(1)).deleteByInstanceAndFrame(sopUid, 0);
    }

    @Test
    void testDeleteByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        doNothing().when(keyImageService).deleteByStudy(studyUid);

        mockMvc.perform(delete("/api/key-images/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isNoContent());

        verify(keyImageService, times(1)).deleteByStudy(studyUid);
    }

    @Test
    void testCountByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(keyImageService.countByStudy(studyUid)).thenReturn(5L);

        mockMvc.perform(get("/api/key-images/study/{studyInstanceUid}/count", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(5));
    }
}
