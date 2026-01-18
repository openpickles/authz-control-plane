package org.openpickles.policy.engine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_actor", columnList = "actorUsername"),
        @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId"),
        @Index(name = "idx_audit_action", columnList = "action")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

    // Actor Details
    @Column(nullable = false)
    private String actorUsername;

    private String actorUserId; // Stable ID if available

    // Request Context (GDPR/Security)
    private String clientIp;
    private String userAgent;
    private String country; // derived from IP if possible
    private String sessionId;
    private String requestId; // Trace ID

    // Action Details
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, SYNC

    @Column(nullable = false)
    private String resourceType; // POLICY, ENTITLEMENT, USER

    private String resourceId;

    private String sensitivityLevel; // PUBLIC, CONFIDENTIAL

    // State Changes (JSON)
    @Column(columnDefinition = "TEXT")
    private String oldValues;

    @Column(columnDefinition = "TEXT")
    private String newValues;

    // Outcome
    private String status; // SUCCESS, FAILURE
    private String failureReason;

    // Integrity
    @Column(nullable = false)
    private String checksum; // SHA-256 Hash for tamper evidence

    public AuditLog() {
    }

    public AuditLog(UUID id, Instant timestamp, String actorUsername, String actorUserId, String clientIp,
            String userAgent, String country, String sessionId, String requestId, String action, String resourceType,
            String resourceId, String sensitivityLevel, String oldValues, String newValues, String status,
            String failureReason, String checksum) {
        this.id = id;
        this.timestamp = timestamp;
        this.actorUsername = actorUsername;
        this.actorUserId = actorUserId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.country = country;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.sensitivityLevel = sensitivityLevel;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.status = status;
        this.failureReason = failureReason;
        this.checksum = checksum;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private UUID id;
        private Instant timestamp;
        private String actorUsername;
        private String actorUserId;
        private String clientIp;
        private String userAgent;
        private String country;
        private String sessionId;
        private String requestId;
        private String action;
        private String resourceType;
        private String resourceId;
        private String sensitivityLevel;
        private String oldValues;
        private String newValues;
        private String status;
        private String failureReason;
        private String checksum;

        AuditLogBuilder() {
        }

        public AuditLogBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AuditLogBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLogBuilder actorUsername(String actorUsername) {
            this.actorUsername = actorUsername;
            return this;
        }

        public AuditLogBuilder actorUserId(String actorUserId) {
            this.actorUserId = actorUserId;
            return this;
        }

        public AuditLogBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public AuditLogBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditLogBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AuditLogBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public AuditLogBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public AuditLogBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public AuditLogBuilder sensitivityLevel(String sensitivityLevel) {
            this.sensitivityLevel = sensitivityLevel;
            return this;
        }

        public AuditLogBuilder oldValues(String oldValues) {
            this.oldValues = oldValues;
            return this;
        }

        public AuditLogBuilder newValues(String newValues) {
            this.newValues = newValues;
            return this;
        }

        public AuditLogBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AuditLogBuilder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public AuditLogBuilder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(id, timestamp, actorUsername, actorUserId, clientIp, userAgent, country, sessionId,
                    requestId, action, resourceType, resourceId, sensitivityLevel, oldValues, newValues, status,
                    failureReason, checksum);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(String sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
