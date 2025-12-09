package com.example.policyengine.controller;

import com.example.policyengine.model.PolicyBinding;
import com.example.policyengine.repository.PolicyBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policy-bindings")
public class PolicyBindingController {

    @Autowired
    private PolicyBindingRepository repository;

    @GetMapping
    public List<PolicyBinding> getAllBindings() {
        return repository.findAll();
    }

    @GetMapping("/search")
    public List<PolicyBinding> getBindingsByResourceType(@RequestParam String resourceType) {
        return repository.findByResourceType(resourceType);
    }

    @PostMapping
    public PolicyBinding createOrUpdateBinding(@RequestBody PolicyBinding binding) {
        // Check if binding exists for this type + context
        return repository.findByResourceTypeAndContext(binding.getResourceType(), binding.getContext())
                .map(existing -> {
                    existing.setPolicyId(binding.getPolicyId());
                    existing.setEvaluationMode(binding.getEvaluationMode());
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(binding));
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
