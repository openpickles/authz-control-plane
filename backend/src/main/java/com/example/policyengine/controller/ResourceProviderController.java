package com.example.policyengine.controller;

import com.example.policyengine.model.ResourceProvider;
import com.example.policyengine.repository.ResourceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/providers")
public class ResourceProviderController {

    @Autowired
    private ResourceProviderRepository repository;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public List<ResourceProvider> getAllProviders() {
        return repository.findAll();
    }

    @PostMapping
    public ResourceProvider createProvider(@RequestBody ResourceProvider provider) {
        return repository.save(provider);
    }

    @DeleteMapping("/{id}")
    public void deleteProvider(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // Simple in-memory cache for schemas
    private final Map<Long, SchemaCacheEntry> schemaCache = new java.util.concurrent.ConcurrentHashMap<>();

    private static class SchemaCacheEntry {
        Object schema;
        long timestamp;

        SchemaCacheEntry(Object schema, long timestamp) {
            this.schema = schema;
            this.timestamp = timestamp;
        }
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<?> getProviderMetadata(@PathVariable Long id) {
        // 1. Check Cache (TTL 1 hour)
        SchemaCacheEntry entry = schemaCache.get(id);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp < 3600000)) {
            return ResponseEntity.ok(entry.schema);
        }

        // 2. Fetch from Provider
        return repository.findById(id)
                .map(provider -> {
                    try {
                        // Try /metadata first, fall back to /schema if needed or just use /metadata as
                        // standard
                        String url = provider.getBaseUrl() + "/metadata";
                        Object metadata = restTemplate.getForObject(url, Object.class);

                        // 3. Update Cache
                        schemaCache.put(id, new SchemaCacheEntry(metadata, System.currentTimeMillis()));
                        return ResponseEntity.ok(metadata);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Error fetching metadata: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Keep legacy schema endpoint for backward compatibility if needed, or redirect
    @GetMapping("/{id}/schema")
    public ResponseEntity<?> getProviderSchema(@PathVariable Long id) {
        return getProviderMetadata(id);
    }

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchResources(@RequestParam String resourceType,
            @RequestParam Map<String, String> allParams) {
        List<ResourceProvider> providers = repository.findByResourceType(resourceType);
        if (providers.isEmpty()) {
            return ResponseEntity.ok(List.of()); // No providers found
        }

        // For simplicity, we'll just query the first provider found for this type
        ResourceProvider provider = providers.get(0);

        // Build URL with query params
        StringBuilder urlBuilder = new StringBuilder(provider.getBaseUrl() + provider.getFetchEndpoint());
        urlBuilder.append("?");

        allParams.forEach((k, v) -> {
            if (!k.equals("resourceType")) { // Exclude our internal param
                urlBuilder.append(k).append("=").append(v).append("&");
            }
        });

        try {
            // Expecting the microservice to return a list of objects or strings
            // We'll return exactly what the microservice returns
            Object response = restTemplate.getForObject(urlBuilder.toString(), Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching resources: " + e.getMessage());
        }
    }

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
    @GetMapping("/mock/metadata")
    public Map<String, Object> getMockMetadata() {
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
