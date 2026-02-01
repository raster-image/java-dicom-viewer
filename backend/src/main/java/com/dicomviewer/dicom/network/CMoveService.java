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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Service for DICOM C-MOVE operations.
 * Used to retrieve studies, series, or instances from remote PACS systems.
 */
@Service
public class CMoveService {

    private static final Logger log = LoggerFactory.getLogger(CMoveService.class);

    // Study Root Query/Retrieve Information Model - MOVE SOP Class
    private static final String STUDY_ROOT_MOVE = "1.2.840.10008.5.1.4.1.2.2.2";

    private final ApplicationEntity applicationEntity;
    private final Device device;

    @Value("${dicom.ae.title:DICOM_VIEWER}")
    private String localAeTitle;

    @Autowired
    public CMoveService(ApplicationEntity applicationEntity, Device device) {
        this.applicationEntity = applicationEntity;
        this.device = device;
    }

    /**
     * Retrieve a study from a remote PACS using C-MOVE.
     *
     * @param pacsConfig      The remote PACS configuration
     * @param studyInstanceUid The Study Instance UID to retrieve
     * @param destinationAe   The destination AE title (usually the local AE)
     * @return MoveResult with status information
     */
    public MoveResult moveStudy(PacsConfiguration pacsConfig, String studyInstanceUid, String destinationAe) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);

        return executeMove(pacsConfig, keys, destinationAe);
    }

    /**
     * Retrieve a series from a remote PACS using C-MOVE.
     */
    public MoveResult moveSeries(PacsConfiguration pacsConfig, String studyInstanceUid,
                                  String seriesInstanceUid, String destinationAe) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        keys.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);

        return executeMove(pacsConfig, keys, destinationAe);
    }

    /**
     * Retrieve a single instance from a remote PACS using C-MOVE.
     */
    public MoveResult moveInstance(PacsConfiguration pacsConfig, String studyInstanceUid,
                                    String seriesInstanceUid, String sopInstanceUid, String destinationAe) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        keys.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
        keys.setString(Tag.SOPInstanceUID, VR.UI, sopInstanceUid);

        return executeMove(pacsConfig, keys, destinationAe);
    }

    /**
     * Execute a C-MOVE operation.
     */
    private MoveResult executeMove(PacsConfiguration pacsConfig, Attributes keys, String destinationAe) {
        Association association = null;
        MoveResult result = new MoveResult();

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
                STUDY_ROOT_MOVE,
                UID.ImplicitVRLittleEndian,
                UID.ExplicitVRLittleEndian
            ));

            // Open association
            association = applicationEntity.connect(remoteConn, rq);

            // Execute C-MOVE
            DimseRSP rsp = association.cmove(STUDY_ROOT_MOVE, Priority.NORMAL, keys, null, destinationAe);

            int completed = 0;
            int failed = 0;
            int warnings = 0;

            while (rsp.next()) {
                Attributes cmd = rsp.getCommand();
                int status = cmd.getInt(Tag.Status, -1);

                if (status == Status.Pending || status == Status.PendingWarning) {
                    // Update progress
                    int remaining = cmd.getInt(Tag.NumberOfRemainingSuboperations, 0);
                    int completedOps = cmd.getInt(Tag.NumberOfCompletedSuboperations, 0);
                    int failedOps = cmd.getInt(Tag.NumberOfFailedSuboperations, 0);
                    int warningOps = cmd.getInt(Tag.NumberOfWarningSuboperations, 0);

                    completed = completedOps;
                    failed = failedOps;
                    warnings = warningOps;

                    log.debug("C-MOVE progress: completed={}, failed={}, remaining={}",
                        completedOps, failedOps, remaining);
                } else if (status == Status.Success) {
                    completed = cmd.getInt(Tag.NumberOfCompletedSuboperations, completed);
                    result.setSuccess(true);
                } else {
                    log.warn("C-MOVE returned status: 0x{}", Integer.toHexString(status));
                    result.setSuccess(false);
                    result.setErrorMessage("C-MOVE failed with status: 0x" + Integer.toHexString(status));
                }
            }

            result.setCompletedSuboperations(completed);
            result.setFailedSuboperations(failed);
            result.setWarningSuboperations(warnings);

            log.info("C-MOVE completed: {} successful, {} failed, {} warnings",
                completed, failed, warnings);

        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
            log.error("C-MOVE failed to {}: {}", pacsConfig.getAeTitle(), e.getMessage());
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            if (association != null && association.isReadyForDataTransfer()) {
                try {
                    association.release();
                } catch (IOException e) {
                    log.warn("Failed to release association", e);
                }
            }
        }

        return result;
    }

    /**
     * Result of a C-MOVE operation.
     */
    public static class MoveResult {
        private boolean success;
        private int completedSuboperations;
        private int failedSuboperations;
        private int warningSuboperations;
        private String errorMessage;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getCompletedSuboperations() {
            return completedSuboperations;
        }

        public void setCompletedSuboperations(int completedSuboperations) {
            this.completedSuboperations = completedSuboperations;
        }

        public int getFailedSuboperations() {
            return failedSuboperations;
        }

        public void setFailedSuboperations(int failedSuboperations) {
            this.failedSuboperations = failedSuboperations;
        }

        public int getWarningSuboperations() {
            return warningSuboperations;
        }

        public void setWarningSuboperations(int warningSuboperations) {
            this.warningSuboperations = warningSuboperations;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
