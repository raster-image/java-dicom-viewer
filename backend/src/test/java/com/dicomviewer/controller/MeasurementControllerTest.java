package com.dicomviewer.controller;

import com.dicomviewer.model.entity.Measurement;
import com.dicomviewer.service.MeasurementService;
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
 * Integration tests for MeasurementController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void testCreateMeasurement() throws Exception {
        when(measurementService.create(any(Measurement.class))).thenReturn(testMeasurement);

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeasurement)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"))
                .andExpect(jsonPath("$.measurementType").value("LENGTH"))
                .andExpect(jsonPath("$.value").value(45.5))
                .andExpect(jsonPath("$.unit").value("mm"));

        verify(measurementService, times(1)).create(any(Measurement.class));
    }

    @Test
    void testGetMeasurementById() throws Exception {
        when(measurementService.getById(testId)).thenReturn(Optional.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.studyInstanceUid").value("1.2.3.4.5"));
    }

    @Test
    void testGetMeasurementByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementService.getById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/measurements/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateMeasurement() throws Exception {
        Measurement updateData = new Measurement();
        updateData.setLabel("Updated Label");
        updateData.setValue(50.0);

        when(measurementService.update(eq(testId), any(Measurement.class))).thenReturn(testMeasurement);

        mockMvc.perform(put("/api/measurements/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());

        verify(measurementService, times(1)).update(eq(testId), any(Measurement.class));
    }

    @Test
    void testUpdateMeasurementNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementService.update(eq(nonExistentId), any(Measurement.class)))
                .thenThrow(new IllegalArgumentException("Measurement not found: " + nonExistentId));

        mockMvc.perform(put("/api/measurements/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeasurement)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testDeleteMeasurement() throws Exception {
        doNothing().when(measurementService).delete(testId);

        mockMvc.perform(delete("/api/measurements/{id}", testId))
                .andExpect(status().isNoContent());

        verify(measurementService, times(1)).delete(testId);
    }

    @Test
    void testGetMeasurementsByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(measurementService.getByStudy(studyUid)).thenReturn(List.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.measurements").isArray())
                .andExpect(jsonPath("$.measurements[0].id").value(testId.toString()));
    }

    @Test
    void testGetMeasurementsByStudyVisibleOnly() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(measurementService.getVisibleByStudy(studyUid)).thenReturn(List.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/study/{studyInstanceUid}", studyUid)
                .param("visibleOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        verify(measurementService, times(1)).getVisibleByStudy(studyUid);
    }

    @Test
    void testGetMeasurementsBySeries() throws Exception {
        String seriesUid = "1.2.3.4.5.6";
        when(measurementService.getBySeries(seriesUid)).thenReturn(List.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/series/{seriesInstanceUid}", seriesUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seriesInstanceUid").value(seriesUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetMeasurementsByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(measurementService.getByInstance(sopUid)).thenReturn(List.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/instance/{sopInstanceUid}", sopUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sopInstanceUid").value(sopUid))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetMeasurementsByInstanceWithFrameIndex() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        when(measurementService.getByInstanceAndFrame(sopUid, 0)).thenReturn(List.of(testMeasurement));

        mockMvc.perform(get("/api/measurements/instance/{sopInstanceUid}", sopUid)
                .param("frameIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frameIndex").value(0));

        verify(measurementService, times(1)).getByInstanceAndFrame(sopUid, 0);
    }

    @Test
    void testToggleVisibility() throws Exception {
        testMeasurement.setVisible(false);
        when(measurementService.toggleVisibility(testId)).thenReturn(testMeasurement);

        mockMvc.perform(post("/api/measurements/{id}/toggle-visibility", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visible").value(false));
    }

    @Test
    void testToggleVisibilityNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(measurementService.toggleVisibility(nonExistentId))
                .thenThrow(new IllegalArgumentException("Measurement not found: " + nonExistentId));

        mockMvc.perform(post("/api/measurements/{id}/toggle-visibility", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testDeleteByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        doNothing().when(measurementService).deleteByStudy(studyUid);

        mockMvc.perform(delete("/api/measurements/study/{studyInstanceUid}", studyUid))
                .andExpect(status().isNoContent());

        verify(measurementService, times(1)).deleteByStudy(studyUid);
    }

    @Test
    void testDeleteBySeries() throws Exception {
        String seriesUid = "1.2.3.4.5.6";
        doNothing().when(measurementService).deleteBySeries(seriesUid);

        mockMvc.perform(delete("/api/measurements/series/{seriesInstanceUid}", seriesUid))
                .andExpect(status().isNoContent());

        verify(measurementService, times(1)).deleteBySeries(seriesUid);
    }

    @Test
    void testDeleteByInstance() throws Exception {
        String sopUid = "1.2.3.4.5.6.7";
        doNothing().when(measurementService).deleteByInstance(sopUid);

        mockMvc.perform(delete("/api/measurements/instance/{sopInstanceUid}", sopUid))
                .andExpect(status().isNoContent());

        verify(measurementService, times(1)).deleteByInstance(sopUid);
    }

    @Test
    void testCountByStudy() throws Exception {
        String studyUid = "1.2.3.4.5";
        when(measurementService.countByStudy(studyUid)).thenReturn(5L);

        mockMvc.perform(get("/api/measurements/study/{studyInstanceUid}/count", studyUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyInstanceUid").value(studyUid))
                .andExpect(jsonPath("$.count").value(5));
    }
}
