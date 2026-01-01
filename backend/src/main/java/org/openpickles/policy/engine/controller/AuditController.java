package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.AuditLog;
import org.openpickles.policy.engine.repository.AuditLogRepository;
import org.openpickles.policy.engine.repository.AuditLogSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<AuditLog> spec = Specification.where(AuditLogSpecifications.hasAction(action))
                .and(AuditLogSpecifications.hasResourceType(resourceType))
                .and(AuditLogSpecifications.hasActorUsername(username))
                .and(AuditLogSpecifications.hasStatus(status))
                .and(AuditLogSpecifications.createdAfter(startDate))
                .and(AuditLogSpecifications.createdBefore(endDate));

        return auditLogRepository.findAll(spec, pageable);
    }
}
