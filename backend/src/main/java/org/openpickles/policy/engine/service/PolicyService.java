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

    public Policy createPolicy(Policy policy) {
        return policyRepository.save(policy);
    }

    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

    public Policy updatePolicy(Long id, Policy policyDetails) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Policy not found with id: " + id, "FUNC_007"));
        policy.setName(policyDetails.getName());
        policy.setContent(policyDetails.getContent());
        policy.setVersion(policyDetails.getVersion());
        policy.setStatus(policyDetails.getStatus());

        policy.setSourceType(policyDetails.getSourceType());
        policy.setGitRepositoryUrl(policyDetails.getGitRepositoryUrl());
        policy.setGitBranch(policyDetails.getGitBranch());
        policy.setGitPath(policyDetails.getGitPath());

        return policyRepository.save(policy);
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

    public void deletePolicy(Long id) {
        policyRepository.deleteById(id);
    }
}
