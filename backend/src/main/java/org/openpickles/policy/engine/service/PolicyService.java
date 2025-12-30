package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.repository.PolicyRepository;
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

    public Policy createPolicy(Policy policy) {
        validatePolicy(policy);
        return policyRepository.save(policy);
    }

    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

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

        return policyRepository.save(policy);
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
        } catch (Exception e) {
            policy.setSyncStatus("PUSH FAILED: " + e.getMessage());
            policyRepository.save(policy);
            throw e;
        }
    }

    public void deletePolicy(Long id) {
        policyRepository.deleteById(id);
    }
}
