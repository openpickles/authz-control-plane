package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.model.ResourceType;
import org.openpickles.policy.engine.repository.ResourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ResourceTypeService {

    @Autowired
    private ResourceTypeRepository repository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Page<ResourceType> getAllResourceTypes(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return repository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return repository.findAll(pageable);
    }

    public List<ResourceType> getAllResourceTypes() {
        return repository.findAll();
    }

    public ResourceType createResourceType(ResourceType resourceType) {
        // Validation could go here
        return repository.save(resourceType);
    }

    public ResourceType updateResourceType(Long id, ResourceType details) {
        ResourceType existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource Type not found with id: " + id));

        existing.setName(details.getName());
        existing.setKey(details.getKey());
        existing.setDescription(details.getDescription());
        existing.setBaseUrl(details.getBaseUrl());
        existing.setDataEndpoint(details.getDataEndpoint());
        existing.setMetadataEndpoint(details.getMetadataEndpoint());

        // Only update schema if provided (manual update), otherwise keep existing or
        // rely on refresh
        if (details.getSchema() != null) {
            existing.setSchema(details.getSchema());
        }

        return repository.save(existing);
    }

    public void deleteResourceType(Long id) {
        repository.deleteById(id);
    }

    public Optional<ResourceType> getResourceTypeById(Long id) {
        return repository.findById(id);
    }

    // Returns the stored schema string (JSON)
    public String getSchema(Long id) {
        ResourceType type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource Type not found with id: " + id));
        return type.getSchema();
    }

    // Fetches schema from external provider and updates DB
    public String refreshSchema(Long id) {
        ResourceType type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource Type not found with id: " + id));

        if (type.getBaseUrl() == null || type.getMetadataEndpoint() == null) {
            throw new RuntimeException("Base URL and Metadata Endpoint are required to refresh schema.");
        }

        String url = type.getBaseUrl() + type.getMetadataEndpoint();
        try {
            // We fetch as String to store exactly what they return (or we could fetch
            // Object and serialize)
            // Fetching as String is safer for storage
            String schemaJson = restTemplate.getForObject(url, String.class);
            type.setSchema(schemaJson);
            repository.save(type);
            return schemaJson;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch schema from provider: " + e.getMessage(), e);
        }
    }

    public Object fetchResources(String key, Map<String, String> params) {
        ResourceType type = repository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Resource Type not found with key: " + key));

        if (type.getBaseUrl() == null || type.getDataEndpoint() == null) {
            // If manual type with no endpoints, return empty list
            return List.of();
        }

        StringBuilder urlBuilder = new StringBuilder(type.getBaseUrl() + type.getDataEndpoint());
        urlBuilder.append("?");

        params.forEach((k, v) -> {
            if (!k.equals("resourceType")) {
                urlBuilder.append(k).append("=").append(v).append("&");
            }
        });

        try {
            return restTemplate.getForObject(urlBuilder.toString(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching resources from " + type.getName() + ": " + e.getMessage());
        }
    }
}
