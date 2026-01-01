package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    // Find the most recent log to chain the hash
    Optional<AuditLog> findTopByOrderByTimestampDesc();
}
