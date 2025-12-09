package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Entitlement;
import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.service.EntitlementService;
import org.openpickles.policy.engine.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private EntitlementService entitlementService;

    @GetMapping("/policies")
    public List<Policy> getActivePolicies() {
        return policyService.getAllPolicies().stream()
                .filter(p -> p.getStatus() == Policy.PolicyStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @GetMapping("/entitlements")
    public List<Entitlement> syncEntitlements() {
        return entitlementService.getAllEntitlements();
    }
}
