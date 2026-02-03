package com.dicomviewer.service;

import com.dicomviewer.dicom.network.CEchoService;
import com.dicomviewer.exception.AENotFoundException;
import com.dicomviewer.exception.DuplicateAETitleException;
import com.dicomviewer.model.EchoResultDTO;
import com.dicomviewer.model.PacsConfiguration;
import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import com.dicomviewer.model.entity.ApplicationEntity.ConnectionStatus;
import com.dicomviewer.repository.ApplicationEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationEntityService.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationEntityServiceTest {

    @Mock
    private ApplicationEntityRepository aeRepository;

    @Mock
    private CEchoService cechoService;

    @InjectMocks
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
    void testCreateAE() {
        when(aeRepository.existsByAeTitle("TEST_PACS")).thenReturn(false);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        ApplicationEntity result = aeService.createAE(testAE);

        assertNotNull(result);
        assertEquals("TEST_PACS", result.getAeTitle());
        assertEquals("pacs.example.com", result.getHostname());
        verify(aeRepository, times(1)).save(testAE);
    }

    @Test
    void testCreateAEDuplicateTitle() {
        when(aeRepository.existsByAeTitle("TEST_PACS")).thenReturn(true);

        assertThrows(DuplicateAETitleException.class, () -> {
            aeService.createAE(testAE);
        });
    }

    @Test
    void testCreateAEWithDefaultFlag() {
        testAE.setDefaultAE(true);
        ApplicationEntity existingDefault = new ApplicationEntity();
        existingDefault.setId(3L);
        existingDefault.setDefaultAE(true);

        when(aeRepository.existsByAeTitle("TEST_PACS")).thenReturn(false);
        when(aeRepository.findDefaultAE()).thenReturn(Optional.of(existingDefault));
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        aeService.createAE(testAE);

        verify(aeRepository, times(2)).save(any(ApplicationEntity.class)); // Once for unsetting default, once for creating
        assertFalse(existingDefault.isDefaultAE());
    }

    @Test
    void testUpdateAE() {
        ApplicationEntity updates = new ApplicationEntity();
        updates.setAeTitle("UPDATED_PACS");
        updates.setHostname("updated.example.com");
        updates.setPort(11114);
        updates.setAeType(AEType.REMOTE_LEGACY);
        updates.setEnabled(true);

        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(aeRepository.existsByAeTitle("UPDATED_PACS")).thenReturn(false);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        ApplicationEntity result = aeService.updateAE(1L, updates);

        assertNotNull(result);
        assertEquals("UPDATED_PACS", result.getAeTitle());
        assertEquals("updated.example.com", result.getHostname());
        verify(aeRepository, times(1)).save(any(ApplicationEntity.class));
    }

    @Test
    void testUpdateAENotFound() {
        when(aeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AENotFoundException.class, () -> {
            aeService.updateAE(999L, testAE);
        });
    }

    @Test
    void testUpdateAEDuplicateTitle() {
        ApplicationEntity updates = new ApplicationEntity();
        updates.setAeTitle("EXISTING_PACS");

        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(aeRepository.existsByAeTitle("EXISTING_PACS")).thenReturn(true);

        assertThrows(DuplicateAETitleException.class, () -> {
            aeService.updateAE(1L, updates);
        });
    }

    @Test
    void testDeleteAE() {
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        doNothing().when(aeRepository).delete(testAE);

        aeService.deleteAE(1L);

        verify(aeRepository, times(1)).delete(testAE);
    }

    @Test
    void testDeleteAENotFound() {
        when(aeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AENotFoundException.class, () -> {
            aeService.deleteAE(999L);
        });
    }

    @Test
    void testDeleteLocalAE() {
        when(aeRepository.findById(2L)).thenReturn(Optional.of(localAE));

        assertThrows(IllegalStateException.class, () -> {
            aeService.deleteAE(2L);
        });
    }

    @Test
    void testGetAllAEs() {
        when(aeRepository.findAll()).thenReturn(List.of(testAE, localAE));

        List<ApplicationEntity> results = aeService.getAllAEs();

        assertEquals(2, results.size());
    }

    @Test
    void testGetEnabledRemoteAEs() {
        when(aeRepository.findByEnabledTrue()).thenReturn(List.of(testAE, localAE));

        List<ApplicationEntity> results = aeService.getEnabledRemoteAEs();

        assertEquals(1, results.size());
        assertEquals(AEType.REMOTE_LEGACY, results.get(0).getAeType());
    }

    @Test
    void testGetAEById() {
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));

        Optional<ApplicationEntity> result = aeService.getAEById(1L);

        assertTrue(result.isPresent());
        assertEquals("TEST_PACS", result.get().getAeTitle());
    }

    @Test
    void testGetAEByIdNotFound() {
        when(aeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ApplicationEntity> result = aeService.getAEById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAEByTitle() {
        when(aeRepository.findByAeTitle("TEST_PACS")).thenReturn(Optional.of(testAE));

        Optional<ApplicationEntity> result = aeService.getAEByTitle("TEST_PACS");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetLocalAE() {
        when(aeRepository.findLocalAE()).thenReturn(Optional.of(localAE));

        ApplicationEntity result = aeService.getLocalAE();

        assertNotNull(result);
        assertEquals(AEType.LOCAL, result.getAeType());
    }

    @Test
    void testGetLocalAENotConfigured() {
        when(aeRepository.findLocalAE()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            aeService.getLocalAE();
        });
    }

    @Test
    void testTestConnectionDetailedSuccess() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(true, 50L, "Connection successful");
        
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(cechoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        EchoResultDTO result = aeService.testConnectionDetailed(1L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(ConnectionStatus.SUCCESS, result.getStatus());
        assertEquals(50L, result.getResponseTimeMs());
        assertEquals("Connection successful", result.getMessage());
        verify(aeRepository, times(1)).save(any(ApplicationEntity.class));
    }

    @Test
    void testTestConnectionDetailedFailure() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(false, 100L, "Connection refused");
        
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(cechoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        EchoResultDTO result = aeService.testConnectionDetailed(1L);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(ConnectionStatus.FAILED, result.getStatus());
        assertEquals("Connection refused", result.getMessage());
    }

    @Test
    void testTestConnectionDetailedNotFound() {
        when(aeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AENotFoundException.class, () -> {
            aeService.testConnectionDetailed(999L);
        });
    }

    @Test
    void testTestConnectionDetailedLocalAE() {
        when(aeRepository.findById(2L)).thenReturn(Optional.of(localAE));

        assertThrows(IllegalArgumentException.class, () -> {
            aeService.testConnectionDetailed(2L);
        });
    }

    @Test
    void testTestConnectionDetailedException() {
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(cechoService.echo(any(PacsConfiguration.class))).thenThrow(new RuntimeException("Network error"));
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        EchoResultDTO result = aeService.testConnectionDetailed(1L);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(ConnectionStatus.FAILED, result.getStatus());
        assertEquals("Network error", result.getMessage());
    }

    @Test
    void testTestConnection() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(true, 50L, "Connection successful");
        
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(cechoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        ConnectionStatus result = aeService.testConnection(1L);

        assertEquals(ConnectionStatus.SUCCESS, result);
    }

    @Test
    void testTestAllConnections() {
        CEchoService.EchoResult echoResult = new CEchoService.EchoResult(true, 50L, "Connection successful");
        
        when(aeRepository.findByEnabledTrue()).thenReturn(List.of(testAE, localAE));
        when(aeRepository.findById(1L)).thenReturn(Optional.of(testAE));
        when(cechoService.echo(any(PacsConfiguration.class))).thenReturn(echoResult);
        when(aeRepository.save(any(ApplicationEntity.class))).thenReturn(testAE);

        aeService.testAllConnections();

        // Only testAE should be tested (not localAE)
        verify(cechoService, times(1)).echo(any(PacsConfiguration.class));
    }
}
