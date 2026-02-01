package com.dicomviewer.dicom.network;

import com.dicomviewer.model.PacsConfiguration;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for DICOM C-ECHO operations.
 * Used to verify connectivity with remote PACS systems.
 */
@Service
public class CEchoService {

    private static final Logger log = LoggerFactory.getLogger(CEchoService.class);

    private final ApplicationEntity applicationEntity;
    private final Device device;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    public CEchoService(ApplicationEntity applicationEntity, Device device) {
        this.applicationEntity = applicationEntity;
        this.device = device;
    }

    /**
     * Performs a C-ECHO to verify connectivity with a remote PACS.
     *
     * @param pacsConfig The PACS configuration to test
     * @return The response time in milliseconds, or -1 if failed
     */
    public EchoResult echo(PacsConfiguration pacsConfig) {
        long startTime = System.currentTimeMillis();
        Association association = null;

        try {
            // Create connection to remote PACS
            Connection remoteConn = new Connection();
            remoteConn.setHostname(pacsConfig.getHost());
            remoteConn.setPort(pacsConfig.getPort());

            // Create association request
            AAssociateRQ rq = new AAssociateRQ();
            rq.setCalledAET(pacsConfig.getAeTitle());
            rq.setCallingAET(applicationEntity.getAETitle());
            rq.addPresentationContext(new PresentationContext(
                1,
                UID.Verification,
                UID.ImplicitVRLittleEndian,
                UID.ExplicitVRLittleEndian
            ));

            // Open association
            association = applicationEntity.connect(remoteConn, rq);

            // Send C-ECHO
            DimseRSP rsp = association.cecho();
            rsp.next();

            Attributes cmd = rsp.getCommand();
            int status = cmd.getInt(Tag.Status, -1);

            long responseTime = System.currentTimeMillis() - startTime;

            if (status == 0) {
                log.info("C-ECHO successful to {} ({}:{}), response time: {}ms",
                    pacsConfig.getAeTitle(), pacsConfig.getHost(), pacsConfig.getPort(), responseTime);
                return new EchoResult(true, responseTime, "Connection successful");
            } else {
                log.warn("C-ECHO to {} returned status: 0x{}", pacsConfig.getAeTitle(), Integer.toHexString(status));
                return new EchoResult(false, responseTime, "Unexpected status: " + status);
            }

        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("C-ECHO failed to {} ({}:{}): {}",
                pacsConfig.getAeTitle(), pacsConfig.getHost(), pacsConfig.getPort(), e.getMessage());
            return new EchoResult(false, responseTime, e.getMessage());
        } finally {
            if (association != null && association.isReadyForDataTransfer()) {
                try {
                    association.release();
                } catch (IOException e) {
                    log.warn("Failed to release association", e);
                }
            }
        }
    }

    /**
     * Result of a C-ECHO operation.
     */
    public static class EchoResult {
        private final boolean success;
        private final long responseTimeMs;
        private final String message;

        public EchoResult(boolean success, long responseTimeMs, String message) {
            this.success = success;
            this.responseTimeMs = responseTimeMs;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getMessage() {
            return message;
        }
    }
}
