package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class AuditLogSpecifications {

    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, cb) -> action == null || action.isEmpty() ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> hasResourceType(String resourceType) {
        return (root, query, cb) -> resourceType == null || resourceType.isEmpty() ? null
                : cb.equal(root.get("resourceType"), resourceType);
    }

    public static Specification<AuditLog> hasActorUsername(String username) {
        return (root, query, cb) -> username == null || username.isEmpty() ? null
                : cb.like(cb.lower(root.get("actorUsername")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<AuditLog> hasStatus(String status) {
        return (root, query, cb) -> status == null || status.isEmpty() ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<AuditLog> createdAfter(Instant start) {
        return (root, query, cb) -> start == null ? null : cb.greaterThanOrEqualTo(root.get("timestamp"), start);
    }

    public static Specification<AuditLog> createdBefore(Instant end) {
        return (root, query, cb) -> end == null ? null : cb.lessThanOrEqualTo(root.get("timestamp"), end);
    }
}
