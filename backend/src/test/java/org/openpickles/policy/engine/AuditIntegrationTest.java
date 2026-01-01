package org.openpickles.policy.engine;

import org.junit.jupiter.api.Test;
import org.openpickles.policy.engine.model.AuditLog;
import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.repository.AuditLogRepository;
import org.openpickles.policy.engine.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuditIntegrationTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @WithMockUser(username = "audit_tester", roles = "ADMIN")
    public void testPolicyCreationCreatesAuditLog() throws InterruptedException {
        // Given
        long initialCount = auditLogRepository.count();
        Policy policy = new Policy();
        policy.setName("Audit Test Policy " + Instant.now().toEpochMilli());
        policy.setFilename("audit_test.rego");
        policy.setContent("package test\ndefault allow = true");
        policy.setVersion("1.0");
        policy.setStatus(Policy.PolicyStatus.ACTIVE);

        // When
        policyService.createPolicy(policy);

        // Then
        // Wait briefly for async audit logging
        Thread.sleep(1000);

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSizeGreaterThan((int) initialCount);

        AuditLog latestLog = logs.get(logs.size() - 1);
        assertThat(latestLog.getAction()).isEqualTo("CREATE");
        assertThat(latestLog.getResourceType()).isEqualTo("POLICY");
        assertThat(latestLog.getActorUsername()).isEqualTo("audit_tester");
        assertThat(latestLog.getChecksum()).isNotNull();
        assertThat(latestLog.getRequestId()).isNotNull();
    }
}
