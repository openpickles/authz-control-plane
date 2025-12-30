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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResourceTypeService {

    private static final int MAX_SCHEMA_SIZE = 100 * 1024; // 100KB
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (resourceType.getSchema() != null) {
            validateSchema(resourceType.getSchema());
        }
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
            validateSchema(details.getSchema());
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
            // Fetch with size limit check
            // Note: RestTemplate.getForObject loads entire response into memory.
            // For rigorous size protection against huge payloads, we should use execute()
            // with a ResponseExtractor.
            // Simplified check after fetch for now as we don't expect GBs. 100KB limit.

            // Better approach: HEAD request first to check Content-Length?
            // Or just fetch and check string length.
            String schemaJson = restTemplate.getForObject(url, String.class);

            if (schemaJson != null && schemaJson.length() > MAX_SCHEMA_SIZE) {
                throw new RuntimeException("Remote schema too large. Limit is " + MAX_SCHEMA_SIZE + " bytes.");
            }

            validateSchema(schemaJson);

            type.setSchema(schemaJson);
            repository.save(type);
            return schemaJson;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch schema from provider: " + e.getMessage(), e);
        }
    }

    private void validateSchema(String schemaJson) {
        if (schemaJson == null || schemaJson.isBlank())
            return;

        if (schemaJson.length() > MAX_SCHEMA_SIZE) {
            throw new RuntimeException("Schema too large. Limit is " + MAX_SCHEMA_SIZE + " bytes.");
        }

        try {
            JsonNode root = objectMapper.readTree(schemaJson);

            // 1. Structure Check
            if (!root.isObject() || !root.has("attributes") || !root.get("attributes").isArray()) {
                throw new RuntimeException("Invalid schema structure. Must contain 'attributes' array.");
            }

            // 2. Attribute Validation
            for (JsonNode attr : root.get("attributes")) {
                if (!attr.has("name") || !attr.has("type") || !attr.has("pii")) {
                    throw new RuntimeException("Missing required attribute fields: name, type, pii");
                }

                String name = attr.get("name").asText();
                if (!name.matches("^[a-zA-Z0-9_]+$")) {
                    throw new RuntimeException("Invalid attribute name: " + name + ". Must be alphanumeric.");
                }

                // Type Check (optional, extendable)
                // String type = attr.get("type").asText();
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Invalid JSON format", e);
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
