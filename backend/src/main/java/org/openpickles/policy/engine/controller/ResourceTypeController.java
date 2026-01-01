package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.ResourceType;
import org.openpickles.policy.engine.service.ResourceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resource-types")
public class ResourceTypeController {

    @Autowired
    private ResourceTypeService service;

    @GetMapping
    public org.springframework.data.domain.Page<ResourceType> getAllResourceTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return service.getAllResourceTypes(pageable, search);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceType> getResourceType(@PathVariable Long id) {
        return service.getResourceTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResourceType createResourceType(@RequestBody ResourceType resourceType) {
        return service.createResourceType(resourceType);
    }

    @PutMapping("/{id}")
    public ResourceType updateResourceType(@PathVariable Long id, @RequestBody ResourceType resourceType) {
        return service.updateResourceType(id, resourceType);
    }

    @DeleteMapping("/{id}")
    public void deleteResourceType(@PathVariable Long id) {
        service.deleteResourceType(id);
    }

    @PostMapping("/{id}/refresh-schema")
    public ResponseEntity<?> refreshSchema(@PathVariable Long id) {
        try {
            String schema = service.refreshSchema(id);
            return ResponseEntity.ok(schema); // Return the raw JSON string or object
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error refreshing schema: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/schema")
    public ResponseEntity<?> getSchema(@PathVariable Long id) {
        try {
            String schema = service.getSchema(id);
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchResources(@RequestParam String resourceType, // This is the 'key'
            @RequestParam Map<String, String> allParams) {
        try {
            return ResponseEntity.ok(service.fetchResources(resourceType, allParams));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // =================================================================================
    // MOCK ENDPOINTS (Kept for Demo/Testing Purposes, functionally same as before)
    // =================================================================================

    // Mock endpoint to simulate a microservice (e.g., Loan Service)
    @GetMapping("/mock/loans")
    public List<Map<String, Object>> getMockLoans(@RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minAmount) {
        List<Map<String, Object>> loans = new ArrayList<>();
        loans.add(Map.of("id", "1001", "name", "Home Loan - John Doe", "status", "ACTIVE", "amount", 150000));
        loans.add(Map.of("id", "1002", "name", "Car Loan - Jane Smith", "status", "ACTIVE", "amount", 25000));
        loans.add(Map.of("id", "1003", "name", "Personal Loan - Bob", "status", "CLOSED", "amount", 5000));
        loans.add(Map.of("id", "1004", "name", "Education Loan - Alice", "status", "ACTIVE", "amount", 12000));

        // Filter logic for mock
        return loans.stream()
                .filter(l -> status == null || status.equalsIgnoreCase((String) l.get("status")))
                .filter(l -> minAmount == null || (Integer) l.get("amount") >= minAmount)
                .collect(java.util.stream.Collectors.toList());
    }

    // Mock Metadata Endpoint (replaces schema)
    @GetMapping("/mock/schema")
    public Map<String, Object> getMockSchema() {
        List<Map<String, Object>> filters = new ArrayList<>();

        filters.add(Map.of(
                "key", "status",
                "label", "Loan Status",
                "type", "select",
                "options", List.of(
                        Map.of("label", "Active", "value", "ACTIVE"),
                        Map.of("label", "Closed", "value", "CLOSED"))));

        filters.add(Map.of(
                "key", "minAmount",
                "label", "Minimum Amount",
                "type", "number"));

        List<String> actions = List.of("read", "write", "approve", "reject", "view_profile");

        return Map.of(
                "filters", filters,
                "actions", actions);
    }
}
