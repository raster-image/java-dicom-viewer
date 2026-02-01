package com.dicomviewer.exception;

/**
 * Exception thrown when attempting to create an AE with a duplicate AE Title.
 */
public class DuplicateAETitleException extends RuntimeException {

    public DuplicateAETitleException(String message) {
        super(message);
    }

    public DuplicateAETitleException(String message, Throwable cause) {
        super(message, cause);
    }
}
