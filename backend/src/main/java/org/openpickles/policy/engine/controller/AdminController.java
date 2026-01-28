package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies")
public class AdminController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private org.openpickles.policy.engine.repository.ServiceRegistryRepository serviceRegistryRepository;

    @GetMapping("/services")
    public ResponseEntity<java.util.List<org.openpickles.policy.engine.model.ServiceRegistry>> getServices() {
        return ResponseEntity.ok(serviceRegistryRepository.findAll());
    }

    @GetMapping("/policies")
    public ResponseEntity<Page<Policy>> getPolicies(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Policy.PolicyOrigin origin,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(policyService.getPolicies(service, origin, search, pageable));
    }

    @PostMapping("/custom")
    public ResponseEntity<Policy> createCustomPolicy(@RequestBody Policy policy) {
        // Enforce CUSTOM origin
        policy.setOrigin(Policy.PolicyOrigin.CUSTOM);
        // Validations
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(policyService.createPolicy(policy));
    }
}
