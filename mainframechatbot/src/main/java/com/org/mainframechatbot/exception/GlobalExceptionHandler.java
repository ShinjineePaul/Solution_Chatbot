package com.org.mainframechatbot.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handler for all REST controllers.
 * Catches every exception and returns a clean JSON error response
 * instead of an HTML stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Standard error response shape returned to the frontend. */
    record ErrorResponse(String message, int status, LocalDateTime timestamp) {
    }

    // Suppress favicon.ico 404 — browser auto-requests this, not a real error
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    // tickets.txt not found → 503
    @ExceptionHandler(TicketFileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(TicketFileNotFoundException ex) {
        log.error("Ticket file error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ex.getMessage(), 503, LocalDateTime.now()));
    }

    // No matching resolved tickets → 404
    @ExceptionHandler(NoMatchingTicketException.class)
    public ResponseEntity<ErrorResponse> handleNoMatch(NoMatchingTicketException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404, LocalDateTime.now()));
    }

    // GPT API failure → 502
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiError(AiServiceException ex) {
        log.error("AI service error: {}", ex.getMessage(), ex.getCause());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("AI service unavailable. Please retry.", 502, LocalDateTime.now()));
    }

    // @Valid annotation failures on request DTOs → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(e -> errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // Catch-all for anything unexpected → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("An unexpected error occurred.", 500, LocalDateTime.now()));
    }
}