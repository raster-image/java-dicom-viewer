package com.dicomviewer.exception;

/**
 * Exception thrown when a DICOM network operation fails.
 */
public class DicomNetworkException extends RuntimeException {

    private final String operation;
    private final String remoteAeTitle;
    private final int status;

    public DicomNetworkException(String message) {
        super(message);
        this.operation = null;
        this.remoteAeTitle = null;
        this.status = -1;
    }

    public DicomNetworkException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.remoteAeTitle = null;
        this.status = -1;
    }

    public DicomNetworkException(String operation, String remoteAeTitle, String message) {
        super(message);
        this.operation = operation;
        this.remoteAeTitle = remoteAeTitle;
        this.status = -1;
    }

    public DicomNetworkException(String operation, String remoteAeTitle, int status, String message) {
        super(message);
        this.operation = operation;
        this.remoteAeTitle = remoteAeTitle;
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public String getRemoteAeTitle() {
        return remoteAeTitle;
    }

    public int getStatus() {
        return status;
    }
}
