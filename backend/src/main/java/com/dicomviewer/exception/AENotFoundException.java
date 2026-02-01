package com.dicomviewer.exception;

/**
 * Exception thrown when an Application Entity is not found.
 */
public class AENotFoundException extends RuntimeException {

    public AENotFoundException(String message) {
        super(message);
    }

    public AENotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
