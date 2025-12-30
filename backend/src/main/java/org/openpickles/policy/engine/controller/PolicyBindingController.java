package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.PolicyBinding;
import org.openpickles.policy.engine.repository.PolicyBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.repository.PolicyRepository;
import org.openpickles.policy.engine.exception.FunctionalException;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/v1/policy-bindings")
public class PolicyBindingController {

    @Autowired
    private PolicyBindingRepository repository;

    @Autowired
    private PolicyRepository policyRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PolicyBindingController.class);

    @GetMapping
    public org.springframework.data.domain.Page<PolicyBinding> getAllBindings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        logger.debug("Fetching bindings, page: {}, size: {}, search: {}", page, size, search);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (search != null && !search.trim().isEmpty()) {
            return repository.findByResourceTypeContainingIgnoreCase(search.trim(), pageable);
        }
        return repository.findAll(pageable);
    }

    @GetMapping("/search")
    public List<PolicyBinding> getBindingsByResourceType(@RequestParam String resourceType) {
        return repository.findByResourceType(resourceType);
    }

    @PostMapping
    public PolicyBinding createBinding(@RequestBody PolicyBinding binding) {
        if (binding.getPolicyIds() == null || binding.getPolicyIds().isEmpty()) {
            throw new FunctionalException(
                    "At least one Policy ID is required", "FUNC_009");
        }

        // validate all policies exist
        List<Policy> policies = policyRepository.findAllById(binding.getPolicyIds());
        if (policies.size() != new HashSet<>(binding.getPolicyIds()).size()) {
            throw new FunctionalException(
                    "One or more policies not found", "FUNC_010");
        }

        return repository.save(binding);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBinding(@PathVariable Long id) {
        return repository.findById(id)
                .map(binding -> {
                    repository.delete(binding);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
