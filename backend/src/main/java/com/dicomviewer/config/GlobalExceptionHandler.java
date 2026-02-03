package com.dicomviewer.config;

import com.dicomviewer.exception.AENotFoundException;
import com.dicomviewer.exception.DuplicateAETitleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler for REST API errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AENotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAENotFound(AENotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
            ));
    }

    @ExceptionHandler(DuplicateAETitleException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateAETitle(DuplicateAETitleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", "DUPLICATE_AE_TITLE",
                "message", ex.getMessage()
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
            ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
            ));
    }
}
