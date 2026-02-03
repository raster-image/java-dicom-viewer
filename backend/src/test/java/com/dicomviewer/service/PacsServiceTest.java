package com.dicomviewer.service;

import com.dicomviewer.dicom.network.CEchoService;
import com.dicomviewer.dicom.network.CFindService;
import com.dicomviewer.dicom.network.CMoveService;
import com.dicomviewer.dicom.web.DicomWebService;
import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.repository.PacsConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PacsService.
 */
@ExtendWith(MockitoExtension.class)
class PacsServiceTest {

    @Mock
    private PacsConfigurationRepository pacsConfigRepository;

    @Mock
    private CEchoService cEchoService;

    @Mock
    private CFindService cFindService;

    @Mock
    private CMoveService cMoveService;

    @Mock
    private DicomWebService dicomWebService;

    @InjectMocks
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
    void testGetAllPacsConfigurations() {
        when(pacsConfigRepository.findAll()).thenReturn(List.of(activePacs, inactivePacs));

        List<PacsConfiguration> results = pacsService.getAllPacsConfigurations();

        assertEquals(2, results.size());
        verify(pacsConfigRepository, times(1)).findAll();
    }

    @Test
    void testGetActivePacsConfigurations() {
        when(pacsConfigRepository.findByIsActiveTrue()).thenReturn(List.of(activePacs));

        List<PacsConfiguration> results = pacsService.getActivePacsConfigurations();

        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsActive());
        verify(pacsConfigRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetPacsConfiguration() {
        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(activePacs));

        Optional<PacsConfiguration> result = pacsService.getPacsConfiguration(activeId);

        assertTrue(result.isPresent());
        assertEquals("Active PACS", result.get().getName());
    }

    @Test
    void testGetPacsConfigurationNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsConfigRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<PacsConfiguration> result = pacsService.getPacsConfiguration(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetPacs() {
        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(activePacs));

        PacsConfiguration result = pacsService.getPacs(activeId);

        assertNotNull(result);
        assertEquals("Active PACS", result.getName());
    }

    @Test
    void testGetPacsNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsConfigRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        PacsConfiguration result = pacsService.getPacs(nonExistentId);

        assertNull(result);
    }

    @Test
    void testGetFirstActivePacs() {
        when(pacsConfigRepository.findByIsActiveTrue()).thenReturn(List.of(activePacs));

        PacsConfiguration result = pacsService.getFirstActivePacs();

        assertNotNull(result);
        assertEquals("Active PACS", result.getName());
    }

    @Test
    void testGetFirstActivePacsEmpty() {
        when(pacsConfigRepository.findByIsActiveTrue()).thenReturn(List.of());

        PacsConfiguration result = pacsService.getFirstActivePacs();

        assertNull(result);
    }

    @Test
    void testCreatePacsConfiguration() {
        when(pacsConfigRepository.save(any(PacsConfiguration.class))).thenReturn(activePacs);

        PacsConfiguration result = pacsService.createPacsConfiguration(activePacs);

        assertNotNull(result);
        assertEquals("Active PACS", result.getName());
        verify(pacsConfigRepository, times(1)).save(activePacs);
    }

    @Test
    void testUpdatePacsConfiguration() {
        PacsConfiguration updates = new PacsConfiguration();
        updates.setName("Updated PACS");
        updates.setHost("updated.example.com");
        updates.setPort(11115);
        updates.setAeTitle("UPDATED_PACS");
        updates.setPacsType(PacsConfiguration.PacsType.LEGACY);
        updates.setIsActive(true);

        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(activePacs));
        when(pacsConfigRepository.save(any(PacsConfiguration.class))).thenReturn(activePacs);

        PacsConfiguration result = pacsService.updatePacsConfiguration(activeId, updates);

        assertNotNull(result);
        assertEquals("Updated PACS", result.getName());
        assertEquals("updated.example.com", result.getHost());
        verify(pacsConfigRepository, times(1)).save(any(PacsConfiguration.class));
    }

    @Test
    void testUpdatePacsConfigurationNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsConfigRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pacsService.updatePacsConfiguration(nonExistentId, activePacs);
        });
    }

    @Test
    void testDeletePacsConfiguration() {
        doNothing().when(pacsConfigRepository).deleteById(activeId);

        pacsService.deletePacsConfiguration(activeId);

        verify(pacsConfigRepository, times(1)).deleteById(activeId);
    }

    @Test
    void testTestConnectionLegacySuccess() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(true, 50L, "Connection successful");

        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(activePacs));
        when(cEchoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);

        Map<String, Object> result = pacsService.testConnection(activeId);

        assertTrue((Boolean) result.get("success"));
        assertEquals(50L, result.get("responseTime"));
        assertEquals("Connection successful", result.get("message"));
    }

    @Test
    void testTestConnectionLegacyFailure() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(false, 100L, "Connection refused");

        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(activePacs));
        when(cEchoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);

        Map<String, Object> result = pacsService.testConnection(activeId);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Connection refused", result.get("message"));
    }

    @Test
    void testTestConnectionNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(pacsConfigRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pacsService.testConnection(nonExistentId);
        });
    }

    @Test
    void testTestConnectionDicomWeb() {
        PacsConfiguration dicomwebPacs = new PacsConfiguration();
        dicomwebPacs.setId(activeId);
        dicomwebPacs.setName("DICOMweb PACS");
        dicomwebPacs.setHost("dicomweb.example.com");
        dicomwebPacs.setPort(8080);
        dicomwebPacs.setAeTitle("DICOMWEB_PACS");
        dicomwebPacs.setPacsType(PacsConfiguration.PacsType.DICOMWEB);
        dicomwebPacs.setQidoRsUrl("https://dicomweb.example.com/qido-rs");
        dicomwebPacs.setIsActive(true);

        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(dicomwebPacs));
        when(dicomWebService.queryStudies(any(PacsConfiguration.class), any())).thenReturn(List.of());

        Map<String, Object> result = pacsService.testConnection(activeId);

        assertTrue((Boolean) result.get("success"));
        assertEquals("Connection successful", result.get("message"));
    }

    @Test
    void testTestConnectionDicomWebFailure() {
        PacsConfiguration dicomwebPacs = new PacsConfiguration();
        dicomwebPacs.setId(activeId);
        dicomwebPacs.setName("DICOMweb PACS");
        dicomwebPacs.setHost("dicomweb.example.com");
        dicomwebPacs.setPort(8080);
        dicomwebPacs.setAeTitle("DICOMWEB_PACS");
        dicomwebPacs.setPacsType(PacsConfiguration.PacsType.DICOMWEB);
        dicomwebPacs.setQidoRsUrl("https://dicomweb.example.com/qido-rs");
        dicomwebPacs.setIsActive(true);

        when(pacsConfigRepository.findById(activeId)).thenReturn(Optional.of(dicomwebPacs));
        when(dicomWebService.queryStudies(any(PacsConfiguration.class), any()))
            .thenThrow(new RuntimeException("Connection failed"));

        Map<String, Object> result = pacsService.testConnection(activeId);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Connection failed", result.get("message"));
    }
}
