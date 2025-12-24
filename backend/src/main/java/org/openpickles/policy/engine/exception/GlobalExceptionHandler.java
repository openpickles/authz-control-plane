package org.openpickles.policy.engine.exception;

import org.openpickles.policy.engine.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ErrorResponse> handleFunctionalException(FunctionalException ex) {
        String traceId = getTraceId();
        logger.warn("Functional error occurred: [{} - {}] TraceId: {}", ex.getErrorCode(), ex.getMessage(), traceId);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnicalException(TechnicalException ex) {
        String traceId = getTraceId();
        logger.error("Technical error occurred: [{} - {}] TraceId: {}", ex.getErrorCode(), ex.getMessage(), traceId,
                ex);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .errorMessage("A technical error occurred. Please contact support.")
                .details(ex.getMessage()) // Ensure secure details in prod (maybe hide this)
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        logger.warn("Validation error: {} TraceId: {}", errorMessage, traceId);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VAL_001")
                .errorMessage("Validation failed")
                .details(errorMessage)
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex) {
        String traceId = getTraceId();
        logger.warn("Data integrity violation: {} TraceId: {}", ex.getMessage(), traceId);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("DAT_001")
                .errorMessage("Policy with this name or filename already exists.")
                .details(ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage())
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        logger.error("Unexpected error occurred: TraceId: {}", traceId, ex);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("GEN_001")
                .errorMessage("An unexpected error occurred.")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID_KEY, traceId);
        }
        return traceId;
    }
}
