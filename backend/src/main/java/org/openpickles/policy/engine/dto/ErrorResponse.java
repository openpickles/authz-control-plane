package org.openpickles.policy.engine.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String details;
    private LocalDateTime timestamp;
    private String traceId;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorCode, String errorMessage, String details, LocalDateTime timestamp,
            String traceId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private String errorCode;
        private String errorMessage;
        private String details;
        private LocalDateTime timestamp;
        private String traceId;

        ErrorResponseBuilder() {
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ErrorResponseBuilder details(String details) {
            this.details = details;
            return this;
        }

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(errorCode, errorMessage, details, timestamp, traceId);
        }
    }
}
