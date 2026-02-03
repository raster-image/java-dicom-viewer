package com.dicomviewer.controller;

import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.service.PacsService;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PacsController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class PacsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacsService pacsService;

    private PacsConfiguration activePacs;
    private PacsConfiguration inactivePacs;
    private UUID activeId;
    private UUID inactiveId;

    @BeforeEach
    void setUp() {
        activeId = UUID.randomUUID();
        inactiveId = UUID.randomUUID();

        activePacs = new PacsConfiguration();
        activePacs.setId(activeId);
        activePacs.setName("Active PACS");
        activePacs.setHost("active.example.com");
        activePacs.setPort(11112);
        activePacs.setAeTitle("ACTIVE_PACS");
        activePacs.setPacsType(PacsConfiguration.PacsType.LEGACY);
        activePacs.setIsActive(true);

        inactivePacs = new PacsConfiguration();
        inactivePacs.setId(inactiveId);
        inactivePacs.setName("Inactive PACS");
        inactivePacs.setHost("inactive.example.com");
        inactivePacs.setPort(11113);
        inactivePacs.setAeTitle("INACTIVE_PACS");
        inactivePacs.setPacsType(PacsConfiguration.PacsType.LEGACY);
        inactivePacs.setIsActive(false);
    }

    @Test
    void testListAllPacs() throws Exception {
        when(pacsService.getAllPacsConfigurations()).thenReturn(List.of(activePacs, inactivePacs));

        mockMvc.perform(get("/api/pacs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurations").isArray())
                .andExpect(jsonPath("$.configurations.length()").value(2))
                .andExpect(jsonPath("$.configurations[0].name").value("Active PACS"))
                .andExpect(jsonPath("$.configurations[1].name").value("Inactive PACS"));

        verify(pacsService, times(1)).getAllPacsConfigurations();
        verify(pacsService, never()).getActivePacsConfigurations();
    }

    @Test
    void testListActiveOnlyPacs() throws Exception {
        when(pacsService.getActivePacsConfigurations()).thenReturn(List.of(activePacs));

        mockMvc.perform(get("/api/pacs").param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurations").isArray())
                .andExpect(jsonPath("$.configurations.length()").value(1))
                .andExpect(jsonPath("$.configurations[0].name").value("Active PACS"));

        verify(pacsService, times(1)).getActivePacsConfigurations();
        verify(pacsService, never()).getAllPacsConfigurations();
    }

    @Test
    void testListPacsWithActiveOnlyFalse() throws Exception {
        when(pacsService.getAllPacsConfigurations()).thenReturn(List.of(activePacs, inactivePacs));

        mockMvc.perform(get("/api/pacs").param("activeOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurations").isArray())
                .andExpect(jsonPath("$.configurations.length()").value(2));

        verify(pacsService, times(1)).getAllPacsConfigurations();
        verify(pacsService, never()).getActivePacsConfigurations();
    }

    @Test
    void testGetPacsById() throws Exception {
        when(pacsService.getPacsConfiguration(activeId)).thenReturn(Optional.of(activePacs));

        mockMvc.perform(get("/api/pacs/{id}", activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeId.toString()))
                .andExpect(jsonPath("$.name").value("Active PACS"))
                .andExpect(jsonPath("$.host").value("active.example.com"))
                .andExpect(jsonPath("$.port").value(11112))
                .andExpect(jsonPath("$.aeTitle").value("ACTIVE_PACS"));
    }

    @Test
    void testGetPacsByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsService.getPacsConfiguration(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pacs/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreatePacs() throws Exception {
        when(pacsService.createPacsConfiguration(any(PacsConfiguration.class))).thenReturn(activePacs);

        mockMvc.perform(post("/api/pacs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activePacs)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Active PACS"))
                .andExpect(jsonPath("$.host").value("active.example.com"));

        verify(pacsService, times(1)).createPacsConfiguration(any(PacsConfiguration.class));
    }

    @Test
    void testUpdatePacs() throws Exception {
        PacsConfiguration updatedPacs = new PacsConfiguration();
        updatedPacs.setId(activeId);
        updatedPacs.setName("Updated PACS");
        updatedPacs.setHost("updated.example.com");
        updatedPacs.setPort(11115);
        updatedPacs.setAeTitle("UPDATED_PACS");
        updatedPacs.setPacsType(PacsConfiguration.PacsType.LEGACY);
        updatedPacs.setIsActive(true);

        when(pacsService.updatePacsConfiguration(eq(activeId), any(PacsConfiguration.class)))
                .thenReturn(updatedPacs);

        mockMvc.perform(put("/api/pacs/{id}", activeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPacs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated PACS"))
                .andExpect(jsonPath("$.host").value("updated.example.com"));

        verify(pacsService, times(1)).updatePacsConfiguration(eq(activeId), any(PacsConfiguration.class));
    }

    @Test
    void testUpdatePacsNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsService.updatePacsConfiguration(eq(nonExistentId), any(PacsConfiguration.class)))
                .thenThrow(new RuntimeException("PACS configuration not found: " + nonExistentId));

        mockMvc.perform(put("/api/pacs/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activePacs)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePacs() throws Exception {
        doNothing().when(pacsService).deletePacsConfiguration(activeId);

        mockMvc.perform(delete("/api/pacs/{id}", activeId))
                .andExpect(status().isNoContent());

        verify(pacsService, times(1)).deletePacsConfiguration(activeId);
    }

    @Test
    void testTestConnectionSuccess() throws Exception {
        Map<String, Object> successResult = Map.of(
                "success", true,
                "responseTime", 50L,
                "message", "Connection successful"
        );

        when(pacsService.testConnection(activeId)).thenReturn(successResult);

        mockMvc.perform(post("/api/pacs/{id}/test", activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.responseTime").value(50))
                .andExpect(jsonPath("$.message").value("Connection successful"));

        verify(pacsService, times(1)).testConnection(activeId);
    }

    @Test
    void testTestConnectionFailure() throws Exception {
        Map<String, Object> failureResult = Map.of(
                "success", false,
                "message", "Connection refused"
        );

        when(pacsService.testConnection(activeId)).thenReturn(failureResult);

        mockMvc.perform(post("/api/pacs/{id}/test", activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Connection refused"));
    }

    @Test
    void testTestConnectionException() throws Exception {
        when(pacsService.testConnection(activeId))
                .thenThrow(new RuntimeException("Network error"));

        mockMvc.perform(post("/api/pacs/{id}/test", activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Network error"));
    }
}
