package com.dicomviewer.controller;

import com.dicomviewer.model.entity.Annotation;
import com.dicomviewer.service.AnnotationService;
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
 * Integration tests for AnnotationController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class AnnotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void testCreateAnnotation() throws Exception {
        when(annotationService.create(any(Annotation.class))).thenReturn(testAnnotation);

        mockMvc.perform(post("/api/annotations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAnnotation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"))
                .andExpect(jsonPath("$.annotationType").value("ARROW"))
                .andExpect(jsonPath("$.text").value("Important finding"));

        verify(annotationService, times(1)).create(any(Annotation.class));
    }

    @Test
    void testGetAnnotationById() throws Exception {
        when(annotationService.getById(testId)).thenReturn(Optional.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"));
    }

    @Test
    void testGetAnnotationByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationService.getById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/annotations/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAnnotation() throws Exception {
        Annotation updateData = new Annotation();
        updateData.setText("Updated text");
        updateData.setColor("#00FF00");

        when(annotationService.update(eq(testId), any(Annotation.class))).thenReturn(testAnnotation);

        mockMvc.perform(put("/api/annotations/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());

        verify(annotationService, times(1)).update(eq(testId), any(Annotation.class));
    }

    @Test
    void testUpdateAnnotationNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationService.update(eq(nonExistentId), any(Annotation.class)))
                .thenThrow(new IllegalArgumentException("Annotation not found: " + nonExistentId));

        mockMvc.perform(put("/api/annotations/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAnnotation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testDeleteAnnotation() throws Exception {
        doNothing().when(annotationService).delete(testId);

        mockMvc.perform(delete("/api/annotations/{id}", testId))
                .andExpect(status().isNoContent());

        verify(annotationService, times(1)).delete(testId);
    }

    @Test
    void testGetAnnotationsByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(annotationService.getByStudy(studyUid)).thenReturn(List.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.annotations").isArray())
                .andExpect(jsonPath("$.annotations[0].id").value(testId.toString()));
    }

    @Test
    void testGetAnnotationsByStudyVisibleOnly() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(annotationService.getVisibleByStudy(studyUid)).thenReturn(List.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/study/{studyInstanceUid}", studyUid)
                .param("visibleOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        verify(annotationService, times(1)).getVisibleByStudy(studyUid);
    }

    @Test
    void testGetAnnotationsBySeries() throws Exception {
        String seriesUid = "1.2.3.4.5.6";
        when(annotationService.getBySeries(seriesUid)).thenReturn(List.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/series/{seriesInstanceUid}", seriesUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seriesInstanceUid").value(seriesUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetAnnotationsByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(annotationService.getByInstance(sopUid)).thenReturn(List.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/instance/{sopInstanceUid}", sopUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sopInstanceUid").value(sopUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetAnnotationsByInstanceWithFrameIndex() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(annotationService.getByInstanceAndFrame(sopUid, 0)).thenReturn(List.of(testAnnotation));

        mockMvc.perform(get("/api/annotations/instance/{sopInstanceUid}", sopUid)
                .param("frameIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frameIndex").value(0));

        verify(annotationService, times(1)).getByInstanceAndFrame(sopUid, 0);
    }

    @Test
    void testToggleVisibility() throws Exception {
        testAnnotation.setVisible(false);
        when(annotationService.toggleVisibility(testId)).thenReturn(testAnnotation);

        mockMvc.perform(post("/api/annotations/{id}/toggle-visibility", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visible").value(false));
    }

    @Test
    void testToggleVisibilityNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationService.toggleVisibility(nonExistentId))
                .thenThrow(new IllegalArgumentException("Annotation not found: " + nonExistentId));

        mockMvc.perform(post("/api/annotations/{id}/toggle-visibility", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testToggleLock() throws Exception {
        testAnnotation.setLocked(true);
        when(annotationService.toggleLock(testId)).thenReturn(testAnnotation);

        mockMvc.perform(post("/api/annotations/{id}/toggle-lock", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    void testToggleLockNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(annotationService.toggleLock(nonExistentId))
                .thenThrow(new IllegalArgumentException("Annotation not found: " + nonExistentId));

        mockMvc.perform(post("/api/annotations/{id}/toggle-lock", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testDeleteByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        doNothing().when(annotationService).deleteByStudy(studyUid);

        mockMvc.perform(delete("/api/annotations/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isNoContent());

        verify(annotationService, times(1)).deleteByStudy(studyUid);
    }

    @Test
    void testDeleteBySeries() throws Exception {
        String seriesUid = "1.2.3.4.5.6";
        doNothing().when(annotationService).deleteBySeries(seriesUid);

        mockMvc.perform(delete("/api/annotations/series/{seriesInstanceUid}", seriesUid))
                .andExpect(status().isNoContent());

        verify(annotationService, times(1)).deleteBySeries(seriesUid);
    }

    @Test
    void testDeleteByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        doNothing().when(annotationService).deleteByInstance(sopUid);

        mockMvc.perform(delete("/api/annotations/instance/{sopInstanceUid}", sopUid))
                .andExpect(status().isNoContent());

        verify(annotationService, times(1)).deleteByInstance(sopUid);
    }

    @Test
    void testCountByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(annotationService.countByStudy(studyUid)).thenReturn(3L);

        mockMvc.perform(get("/api/annotations/study/{studyInstanceUid}/count", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(3));
    }
}
