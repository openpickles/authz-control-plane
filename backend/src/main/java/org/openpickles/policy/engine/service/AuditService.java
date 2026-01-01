package org.openpickles.policy.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpickles.policy.engine.model.AuditLog;
import org.openpickles.policy.engine.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Asynchronously creates and saves an audit log entry.
     * This method is transactional to ensure the chain integrity is maintained
     * (though in high-concurrency
     * this simple chaining might need distributed locking or a queue-based
     * approach; adequate for this single-node app).
     */
    @Async
    @Transactional
    public void log(AuditLog.AuditLogBuilder builder) {
        try {
            AuditLog logEntry = builder.timestamp(Instant.now()).build();

            // Generate Checksum
            String previousChecksum = getPreviousChecksum();
            String currentChecksum = calculateChecksum(logEntry, previousChecksum);
            logEntry.setChecksum(currentChecksum);

            auditLogRepository.save(logEntry);
            log.debug("Audit log saved: {} - {}", logEntry.getAction(), logEntry.getResourceType());

        } catch (Exception e) {
            log.error("FAILED TO SAVE AUDIT LOG: Critical System Failure", e);
            // In a real high-security system, you might stop the world here if auditing
            // fails.
        }
    }

    private String getPreviousChecksum() {
        Optional<AuditLog> lastLog = auditLogRepository.findTopByOrderByTimestampDesc();
        return lastLog.map(AuditLog::getChecksum).orElse("GENESIS_HASH_0000000000000000000000000000");
    }

    private String calculateChecksum(AuditLog log, String previousHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = previousHash +
                    log.getTimestamp().toString() +
                    log.getActorUsername() +
                    log.getAction() +
                    log.getResourceType() +
                    (log.getResourceId() != null ? log.getResourceId() : "") +
                    (log.getClientIp() != null ? log.getClientIp() : "") +
                    (log.getStatus() != null ? log.getStatus() : "");

            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
