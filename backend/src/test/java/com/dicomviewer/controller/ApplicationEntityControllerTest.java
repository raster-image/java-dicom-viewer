package com.dicomviewer.controller;

import com.dicomviewer.exception.AENotFoundException;
import com.dicomviewer.exception.DuplicateAETitleException;
import com.dicomviewer.model.EchoResultDTO;
import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import com.dicomviewer.service.ApplicationEntityService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ApplicationEntityController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class ApplicationEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationEntityService aeService;

    private ApplicationEntity testAE;
    private ApplicationEntity localAE;

    @BeforeEach
    void setUp() {
        testAE = new ApplicationEntity();
        testAE.setId(1L);
        testAE.setAeTitle("TEST_PACS");
        testAE.setHostname("pacs.example.com");
        testAE.setPort(11112);
        testAE.setAeType(AEType.REMOTE_LEGACY);
        testAE.setDescription("Test PACS Server");
        testAE.setEnabled(true);
        testAE.setDefaultAE(false);
        testAE.setConnectionTimeout(30000);
        testAE.setResponseTimeout(60000);
        testAE.setMaxAssociations(10);

        localAE = new ApplicationEntity();
        localAE.setId(2L);
        localAE.setAeTitle("LOCAL_AE");
        localAE.setHostname("localhost");
        localAE.setPort(11113);
        localAE.setAeType(AEType.LOCAL);
        localAE.setEnabled(true);
    }

    @Test
    void testGetAllAEs() throws Exception {
        when(aeService.getAllAEs()).thenReturn(List.of(testAE, localAE));

        mockMvc.perform(get("/api/ae"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].aeTitle").value("TEST_PACS"))
                .andExpect(jsonPath("$[1].aeTitle").value("LOCAL_AE"));

        verify(aeService, times(1)).getAllAEs();
    }

    @Test
    void testGetAEById() throws Exception {
        when(aeService.getAEById(1L)).thenReturn(Optional.of(testAE));

        mockMvc.perform(get("/api/ae/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.aeTitle").value("TEST_PACS"))
                .andExpect(jsonPath("$.hostname").value("pacs.example.com"))
                .andExpect(jsonPath("$.port").value(11112))
                .andExpect(jsonPath("$.aeType").value("REMOTE_LEGACY"));
    }

    @Test
    void testGetAEByIdNotFound() throws Exception {
        when(aeService.getAEById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ae/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateAE() throws Exception {
        when(aeService.createAE(any(ApplicationEntity.class))).thenReturn(testAE);

        mockMvc.perform(post("/api/ae")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.aeTitle").value("TEST_PACS"))
                .andExpect(jsonPath("$.hostname").value("pacs.example.com"));

        verify(aeService, times(1)).createAE(any(ApplicationEntity.class));
    }

    @Test
    void testCreateAEDuplicateTitle() throws Exception {
        when(aeService.createAE(any(ApplicationEntity.class)))
                .thenThrow(new DuplicateAETitleException("AE Title 'TEST_PACS' already exists"));

        mockMvc.perform(post("/api/ae")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateAE() throws Exception {
        ApplicationEntity updatedAE = new ApplicationEntity();
        updatedAE.setId(1L);
        updatedAE.setAeTitle("UPDATED_PACS");
        updatedAE.setHostname("updated.example.com");
        updatedAE.setPort(11114);
        updatedAE.setAeType(AEType.REMOTE_LEGACY);
        updatedAE.setEnabled(true);

        when(aeService.updateAE(eq(1L), any(ApplicationEntity.class))).thenReturn(updatedAE);

        mockMvc.perform(put("/api/ae/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aeTitle").value("UPDATED_PACS"))
                .andExpect(jsonPath("$.hostname").value("updated.example.com"));

        verify(aeService, times(1)).updateAE(eq(1L), any(ApplicationEntity.class));
    }

    @Test
    void testUpdateAENotFound() throws Exception {
        when(aeService.updateAE(eq(999L), any(ApplicationEntity.class)))
                .thenThrow(new AENotFoundException("AE with id 999 not found"));

        mockMvc.perform(put("/api/ae/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAE() throws Exception {
        doNothing().when(aeService).deleteAE(1L);

        mockMvc.perform(delete("/api/ae/1"))
                .andExpect(status().isNoContent());

        verify(aeService, times(1)).deleteAE(1L);
    }

    @Test
    void testDeleteAENotFound() throws Exception {
        doThrow(new AENotFoundException("AE with id 999 not found"))
                .when(aeService).deleteAE(999L);

        mockMvc.perform(delete("/api/ae/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteLocalAE() throws Exception {
        doThrow(new IllegalStateException("Cannot delete local AE configuration"))
                .when(aeService).deleteAE(2L);

        mockMvc.perform(delete("/api/ae/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTestConnectionSuccess() throws Exception {
        EchoResultDTO echoResult = new EchoResultDTO(
                1L, "TEST_PACS", "pacs.example.com", 11112,
                ConnectionStatus.SUCCESS, true, 50L, "Connection successful"
        );

        when(aeService.testConnectionDetailed(1L)).thenReturn(echoResult);

        mockMvc.perform(post("/api/ae/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aeId").value(1))
                .andExpect(jsonPath("$.aeTitle").value("TEST_PACS"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.responseTimeMs").value(50))
                .andExpect(jsonPath("$.message").value("Connection successful"));

        verify(aeService, times(1)).testConnectionDetailed(1L);
    }

    @Test
    void testTestConnectionFailure() throws Exception {
        EchoResultDTO echoResult = new EchoResultDTO(
                1L, "TEST_PACS", "pacs.example.com", 11112,
                ConnectionStatus.FAILED, false, 100L, "Connection refused"
        );

        when(aeService.testConnectionDetailed(1L)).thenReturn(echoResult);

        mockMvc.perform(post("/api/ae/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Connection refused"));
    }

    @Test
    void testTestConnectionNotFound() throws Exception {
        when(aeService.testConnectionDetailed(999L))
                .thenThrow(new AENotFoundException("AE with id 999 not found"));

        mockMvc.perform(post("/api/ae/999/test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testTestConnectionLocalAE() throws Exception {
        when(aeService.testConnectionDetailed(2L))
                .thenThrow(new IllegalArgumentException("Cannot test connection to local AE"));

        mockMvc.perform(post("/api/ae/2/test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTestAllConnections() throws Exception {
        doNothing().when(aeService).testAllConnections();

        mockMvc.perform(post("/api/ae/test-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Connection tests initiated"));

        verify(aeService, times(1)).testAllConnections();
    }
}
