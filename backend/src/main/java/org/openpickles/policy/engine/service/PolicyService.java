package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.repository.PolicyRepository;
import org.openpickles.policy.engine.aop.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private GitService gitService;

    @Autowired
    private org.openpickles.policy.engine.event.EventPublisher eventPublisher;

    @Autowired
    private org.openpickles.policy.engine.repository.PolicyBindingRepository policyBindingRepository;

    @Autowired
    private org.openpickles.policy.engine.repository.PolicyBundleRepository policyBundleRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public org.springframework.data.domain.Page<Policy> getAllPolicies(
            org.springframework.data.domain.Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return policyRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return policyRepository.findAll(pageable);
    }

    @Auditable(action = "CREATE", resourceType = "POLICY")
    public Policy createPolicy(Policy policy) {
        validatePolicy(policy);
        return policyRepository.save(policy);
    }

    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

    @Auditable(action = "UPDATE", resourceType = "POLICY")
    public Policy updatePolicy(Long id, Policy policyDetails) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Policy not found with id: " + id, "FUNC_007"));

        validatePolicy(policyDetails);

        policy.setName(policyDetails.getName());
        policy.setContent(policyDetails.getContent());
        policy.setVersion(policyDetails.getVersion());
        policy.setStatus(policyDetails.getStatus());

        policy.setSourceType(policyDetails.getSourceType());
        policy.setGitRepositoryUrl(policyDetails.getGitRepositoryUrl());
        policy.setGitBranch(policyDetails.getGitBranch());
        policy.setGitPath(policyDetails.getGitPath());
        policy.setFilename(policyDetails.getFilename());

        Policy saved = policyRepository.save(policy);
        notifyPolicyChange(saved);
        return saved;
    }

    private void notifyPolicyChange(Policy policy) {
        try {
            // Find all bindings that use this policy
            List<org.openpickles.policy.engine.model.PolicyBinding> bindings = policyBindingRepository
                    .findByPolicyIdsContaining(policy.getId());

            for (org.openpickles.policy.engine.model.PolicyBinding binding : bindings) {
                // Find all bundles that use this binding
                List<org.openpickles.policy.engine.model.PolicyBundle> bundles = policyBundleRepository
                        .findByBindingIdsContaining(binding.getId());

                for (org.openpickles.policy.engine.model.PolicyBundle bundle : bundles) {
                    // Construct Event Data
                    java.util.Map<String, String> eventData = new java.util.HashMap<>();
                    eventData.put("bundleName", bundle.getName());
                    eventData.put("version", java.util.UUID.randomUUID().toString()); // Mock version for now
                    eventData.put("downloadUrl", "/api/v1/bundles/" + bundle.getName() + "/download"); // TODO: Real URL

                    byte[] dataBytes = objectMapper.writeValueAsBytes(eventData);

                    // Build CloudEvent
                    io.cloudevents.CloudEvent event = io.cloudevents.core.builder.CloudEventBuilder.v1()
                            .withId(java.util.UUID.randomUUID().toString())
                            .withSource(java.net.URI.create("/policy-engine/control-plane"))
                            .withType("org.openpickles.policy.bundle.update")
                            .withSubject("bundles/" + bundle.getName())
                            .withTime(java.time.OffsetDateTime.now())
                            .withDataContentType("application/json")
                            .withData(dataBytes)
                            .build();

                    // Publish
                    eventPublisher.publish("bundles/" + bundle.getName(), event);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to publish policy update event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validatePolicy(Policy policy) {
        if (policy.getName() == null || policy.getName().trim().isEmpty()) {
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Policy name cannot be empty", "VAL_001");
        }
        if (policy.getFilename() == null || policy.getFilename().trim().isEmpty()) {
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Filename cannot be empty", "VAL_002");
        }
    }

    @Auditable(action = "SYNC", resourceType = "POLICY")
    public Policy syncPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Policy not found with id: " + id, "FUNC_007"));

        if (policy.getSourceType() != Policy.SourceType.GIT) {
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Policy is not configured for Git sync", "FUNC_008");
        }

        try {
            String content = gitService.fetchFileContent(
                    policy.getGitRepositoryUrl(),
                    policy.getGitBranch(),
                    policy.getGitPath());

            policy.setContent(content);
            policy.setLastSyncTime(LocalDateTime.now());
            policy.setSyncStatus("SUCCESS");
        } catch (Exception e) {
            policy.setSyncStatus("FAILED: " + e.getMessage());
            policyRepository.save(policy); // Save status even if failed
            throw e;
        }

        return policyRepository.save(policy);
    }

    @Auditable(action = "PUSH_TO_GIT", resourceType = "POLICY")
    public void pushToGit(Long id, String commitMessage) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Policy not found with id: " + id, "FUNC_007"));

        if (policy.getSourceType() != Policy.SourceType.GIT) {
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Policy is not configured for Git sync", "FUNC_008");
        }

        try {
            gitService.pushFileContent(
                    policy.getGitRepositoryUrl(),
                    policy.getGitBranch(),
                    policy.getGitPath(),
                    policy.getContent(),
                    commitMessage);
            policy.setLastSyncTime(LocalDateTime.now());
            policy.setSyncStatus("SUCCESS (Pushed)");
            policyRepository.save(policy);
            notifyPolicyChange(policy); // Notify after push too? Or only after Sync? Assuming update here implies
                                        // change.
        } catch (Exception e) {
            policy.setSyncStatus("PUSH FAILED: " + e.getMessage());
            policyRepository.save(policy);
            throw e;
        }
    }

    @Auditable(action = "DELETE", resourceType = "POLICY")
    public void deletePolicy(Long id) {
        policyRepository.deleteById(id);
    }
}
