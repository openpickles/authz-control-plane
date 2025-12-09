package org.openpickles.policy.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String details;
    private LocalDateTime timestamp;
    private String traceId;
}
