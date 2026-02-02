package com.dicomviewer.config;

import jakarta.annotation.PreDestroy;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * DICOM configuration for network operations using dcm4che.
 * Configures the local Application Entity (AE) for DICOM operations.
 */
@Configuration
public class DicomConfig {

    private static final Logger log = LoggerFactory.getLogger(DicomConfig.class);

    @Value("${dicom.ae.title:DICOM_VIEWER}")
    private String aeTitle;

    @Value("${dicom.ae.port:11112}")
    private int aePort;

    @Value("${dicom.ae.enabled:true}")
    private boolean aeEnabled;

    private Device device;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutor;

    /**
     * Creates the DICOM device for this application.
     * Only starts if dicom.ae.enabled=true
     */
    @Bean
    public Device dicomDevice() throws IOException, GeneralSecurityException {
        if (!aeEnabled) {
            log.info("Local DICOM AE is disabled - skipping device initialization");
            // Create minimal device without binding connections
            device = new Device("dicom-viewer");
            executor = Executors.newCachedThreadPool();
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);

            // Create connection but don't bind
            Connection conn = new Connection();
            conn.setPort(aePort);
            conn.setHostname("0.0.0.0");
            device.addConnection(conn);

            // Create Application Entity (needed for outgoing connections)
            ApplicationEntity ae = new ApplicationEntity(aeTitle);
            ae.setAssociationAcceptor(false);
            ae.setAssociationInitiator(true);
            ae.addConnection(conn);
            addTransferCapabilities(ae);
            device.addApplicationEntity(ae);

            return device;
        }
        device = new Device("dicom-viewer");

        // Initialize executor services
        executor = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);

        // Create connection for incoming associations
        Connection conn = new Connection();
        conn.setPort(aePort);
        conn.setHostname("0.0.0.0");
        device.addConnection(conn);

        // Create Application Entity
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        ae.addConnection(conn);

        // Add transfer capabilities
        addTransferCapabilities(ae);

        device.addApplicationEntity(ae);

        // Note: Service registry setup removed as BasicCEchoSCP and DicomServiceRegistry
        // are not available in dcm4che 5.x. Custom DIMSE handlers can be added as needed.

        // Bind connections
        try {
            device.bindConnections();
            log.info("DICOM device started - AE Title: {}, Port: {}", aeTitle, aePort);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to bind DICOM device connections", e);
            throw e;
        }

        return device;
    }

    /**
     * Creates the Application Entity bean.
     */
    @Bean
    public ApplicationEntity applicationEntity(Device device) {
        return device.getApplicationEntity(aeTitle);
    }

    /**
     * Add transfer capabilities for various SOP classes.
     */
    private void addTransferCapabilities(ApplicationEntity ae) {
        // Verification SOP Class (C-ECHO)
        String[] TRANSFER_SYNTAXES = {
            "1.2.840.10008.1.2",      // Implicit VR Little Endian
            "1.2.840.10008.1.2.1",    // Explicit VR Little Endian
            "1.2.840.10008.1.2.2"     // Explicit VR Big Endian
        };

        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.1.1", // Verification SOP Class
            TransferCapability.Role.SCP,
            TRANSFER_SYNTAXES));

        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.1.1", // Verification SOP Class
            TransferCapability.Role.SCU,
            TRANSFER_SYNTAXES));

        // Study Root Query/Retrieve SOP Classes
        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.5.1.4.1.2.2.1", // Study Root Query/Retrieve - FIND
            TransferCapability.Role.SCU,
            TRANSFER_SYNTAXES));

        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.5.1.4.1.2.2.2", // Study Root Query/Retrieve - MOVE
            TransferCapability.Role.SCU,
            TRANSFER_SYNTAXES));

        // Patient Root Query/Retrieve SOP Classes
        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.5.1.4.1.2.1.1", // Patient Root Query/Retrieve - FIND
            TransferCapability.Role.SCU,
            TRANSFER_SYNTAXES));

        ae.addTransferCapability(new TransferCapability(null,
            "1.2.840.10008.5.1.4.1.2.1.2", // Patient Root Query/Retrieve - MOVE
            TransferCapability.Role.SCU,
            TRANSFER_SYNTAXES));

        // Storage SOP Classes (C-STORE SCP for receiving images)
        String[] STORAGE_SOP_CLASSES = {
            "1.2.840.10008.5.1.4.1.1.2",     // CT Image Storage
            "1.2.840.10008.5.1.4.1.1.4",     // MR Image Storage
            "1.2.840.10008.5.1.4.1.1.1",     // CR Image Storage
            "1.2.840.10008.5.1.4.1.1.1.1",   // Digital X-Ray Image Storage
            "1.2.840.10008.5.1.4.1.1.7",     // Secondary Capture Image Storage
            "1.2.840.10008.5.1.4.1.1.6.1",   // US Image Storage
            "1.2.840.10008.5.1.4.1.1.12.1",  // XA Image Storage
            "1.2.840.10008.5.1.4.1.1.20"     // NM Image Storage
        };

        String[] IMAGE_TRANSFER_SYNTAXES = {
            "1.2.840.10008.1.2",              // Implicit VR Little Endian
            "1.2.840.10008.1.2.1",            // Explicit VR Little Endian
            "1.2.840.10008.1.2.4.70",         // JPEG Lossless
            "1.2.840.10008.1.2.4.57",         // JPEG Lossless (Process 14)
            "1.2.840.10008.1.2.4.50",         // JPEG Baseline
            "1.2.840.10008.1.2.4.90",         // JPEG 2000 Lossless
            "1.2.840.10008.1.2.4.91"          // JPEG 2000 Lossy
        };

        for (String sopClass : STORAGE_SOP_CLASSES) {
            ae.addTransferCapability(new TransferCapability(null,
                sopClass,
                TransferCapability.Role.SCP,
                IMAGE_TRANSFER_SYNTAXES));
        }
    }


    @PreDestroy
    public void stopDevice() {
        if (device != null) {
            try {
                device.unbindConnections();
                log.info("DICOM device stopped");
            } catch (Exception e) {
                log.warn("Error unbinding DICOM device connections: {}", e.getMessage());
            }
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }
}
