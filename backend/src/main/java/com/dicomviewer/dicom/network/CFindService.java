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
import java.util.ArrayList;
import java.util.List;

/**
 * Service for DICOM C-FIND operations.
 * Used to query studies, series, and instances from remote PACS systems.
 */
@Service
public class CFindService {

    private static final Logger log = LoggerFactory.getLogger(CFindService.class);

    // Study Root Query/Retrieve Information Model - FIND SOP Class
    private static final String STUDY_ROOT_FIND = "1.2.840.10008.5.1.4.1.2.2.1";

    // Patient Root Query/Retrieve Information Model - FIND SOP Class
    private static final String PATIENT_ROOT_FIND = "1.2.840.10008.5.1.4.1.2.1.1";

    private final ApplicationEntity applicationEntity;
    private final Device device;

    @Autowired
    public CFindService(ApplicationEntity applicationEntity, Device device) {
        this.applicationEntity = applicationEntity;
        this.device = device;
    }

    /**
     * Query studies from a remote PACS.
     */
    public List<Attributes> findStudies(PacsConfiguration pacsConfig, StudyQuery query) {
        Attributes keys = buildStudyQueryKeys(query);
        return executeFind(pacsConfig, keys, QueryLevel.STUDY, STUDY_ROOT_FIND);
    }

    /**
     * Query series from a remote PACS.
     */
    public List<Attributes> findSeries(PacsConfiguration pacsConfig, String studyInstanceUid) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        keys.setNull(Tag.SeriesInstanceUID, VR.UI);
        keys.setNull(Tag.SeriesNumber, VR.IS);
        keys.setNull(Tag.SeriesDescription, VR.LO);
        keys.setNull(Tag.Modality, VR.CS);
        keys.setNull(Tag.NumberOfSeriesRelatedInstances, VR.IS);
        keys.setNull(Tag.BodyPartExamined, VR.CS);

        return executeFind(pacsConfig, keys, QueryLevel.SERIES, STUDY_ROOT_FIND);
    }

    /**
     * Query instances from a remote PACS.
     */
    public List<Attributes> findInstances(PacsConfiguration pacsConfig, String studyInstanceUid, String seriesInstanceUid) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
        keys.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
        keys.setNull(Tag.SOPInstanceUID, VR.UI);
        keys.setNull(Tag.SOPClassUID, VR.UI);
        keys.setNull(Tag.InstanceNumber, VR.IS);
        keys.setNull(Tag.Rows, VR.US);
        keys.setNull(Tag.Columns, VR.US);

        return executeFind(pacsConfig, keys, QueryLevel.IMAGE, STUDY_ROOT_FIND);
    }

    /**
     * Execute a C-FIND operation.
     */
    private List<Attributes> executeFind(PacsConfiguration pacsConfig, Attributes keys, QueryLevel level, String sopClass) {
        List<Attributes> results = new ArrayList<>();
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
                sopClass,
                UID.ImplicitVRLittleEndian,
                UID.ExplicitVRLittleEndian
            ));

            // Open association
            association = applicationEntity.connect(remoteConn, rq);

            // Execute C-FIND
            DimseRSP rsp = association.cfind(sopClass, Priority.NORMAL, keys, null, 0);

            while (rsp.next()) {
                Attributes cmd = rsp.getCommand();
                Attributes data = rsp.getDataset();

                int status = cmd.getInt(Tag.Status, -1);

                // Status 0xFF00 = Pending, 0x0000 = Success
                if (status == Status.Pending || status == Status.PendingWarning) {
                    if (data != null) {
                        results.add(data);
                    }
                } else if (status != Status.Success) {
                    log.warn("C-FIND returned status: 0x{}", Integer.toHexString(status));
                }
            }

            log.info("C-FIND completed with {} results from {} at {} level",
                results.size(), pacsConfig.getAeTitle(), level);

        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
            log.error("C-FIND failed to {}: {}", pacsConfig.getAeTitle(), e.getMessage());
            throw new RuntimeException("C-FIND operation failed: " + e.getMessage(), e);
        } finally {
            if (association != null && association.isReadyForDataTransfer()) {
                try {
                    association.release();
                } catch (IOException e) {
                    log.warn("Failed to release association", e);
                }
            }
        }

        return results;
    }

    /**
     * Build query keys for study-level query.
     */
    private Attributes buildStudyQueryKeys(StudyQuery query) {
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");

        // Required return keys
        keys.setNull(Tag.StudyInstanceUID, VR.UI);
        keys.setNull(Tag.PatientID, VR.LO);
        keys.setNull(Tag.PatientName, VR.PN);
        keys.setNull(Tag.PatientBirthDate, VR.DA);
        keys.setNull(Tag.PatientSex, VR.CS);
        keys.setNull(Tag.StudyDate, VR.DA);
        keys.setNull(Tag.StudyTime, VR.TM);
        keys.setNull(Tag.StudyDescription, VR.LO);
        keys.setNull(Tag.AccessionNumber, VR.SH);
        keys.setNull(Tag.ModalitiesInStudy, VR.CS);
        keys.setNull(Tag.NumberOfStudyRelatedSeries, VR.IS);
        keys.setNull(Tag.NumberOfStudyRelatedInstances, VR.IS);
        keys.setNull(Tag.ReferringPhysicianName, VR.PN);

        // Add query filters
        if (query.getPatientId() != null && !query.getPatientId().isEmpty()) {
            keys.setString(Tag.PatientID, VR.LO, query.getPatientId());
        }
        if (query.getPatientName() != null && !query.getPatientName().isEmpty()) {
            keys.setString(Tag.PatientName, VR.PN, query.getPatientName());
        }
        if (query.getStudyDate() != null && !query.getStudyDate().isEmpty()) {
            keys.setString(Tag.StudyDate, VR.DA, query.getStudyDate());
        }
        if (query.getModality() != null && !query.getModality().isEmpty()) {
            keys.setString(Tag.ModalitiesInStudy, VR.CS, query.getModality());
        }
        if (query.getAccessionNumber() != null && !query.getAccessionNumber().isEmpty()) {
            keys.setString(Tag.AccessionNumber, VR.SH, query.getAccessionNumber());
        }

        return keys;
    }

    /**
     * Query level enumeration.
     */
    public enum QueryLevel {
        PATIENT, STUDY, SERIES, IMAGE
    }

    /**
     * Study query parameters.
     */
    public static class StudyQuery {
        private String patientId;
        private String patientName;
        private String studyDate;
        private String modality;
        private String accessionNumber;

        public String getPatientId() {
            return patientId;
        }

        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public String getPatientName() {
            return patientName;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public String getStudyDate() {
            return studyDate;
        }

        public void setStudyDate(String studyDate) {
            this.studyDate = studyDate;
        }

        public String getModality() {
            return modality;
        }

        public void setModality(String modality) {
            this.modality = modality;
        }

        public String getAccessionNumber() {
            return accessionNumber;
        }

        public void setAccessionNumber(String accessionNumber) {
            this.accessionNumber = accessionNumber;
        }
    }
}
