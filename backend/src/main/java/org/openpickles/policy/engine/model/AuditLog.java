package org.openpickles.policy.engine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_actor", columnList = "actorUsername"),
        @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
