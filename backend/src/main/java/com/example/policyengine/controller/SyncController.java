package com.example.policyengine.controller;

import com.example.policyengine.model.Entitlement;
import com.example.policyengine.model.Policy;
import com.example.policyengine.service.EntitlementService;
import com.example.policyengine.service.PolicyService;
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
