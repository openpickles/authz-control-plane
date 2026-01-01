package org.openpickles.policy.engine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resource_types")
public class ResourceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // previously serviceName

    @Column(nullable = false, unique = true, name = "resource_key")
    private String key; // previously resourceType (e.g. loan-service:loan)

    @Column(length = 1000)
    private String description;

    @Column(nullable = true)
    private String baseUrl;

    @Column(nullable = true)
    private String dataEndpoint; // previously fetchEndpoint

    @Column(nullable = true)
    private String metadataEndpoint; // New: for fetching schema

    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT", name = "schema_definition")
    private String schema; // New: The JSON Schema for filters

    public ResourceType() {
    }

    public ResourceType(String name, String key, String baseUrl, String dataEndpoint) {
        this.name = name;
        this.key = key;
        this.baseUrl = baseUrl;
        this.dataEndpoint = dataEndpoint;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDataEndpoint() {
        return dataEndpoint;
    }

    public void setDataEndpoint(String dataEndpoint) {
        this.dataEndpoint = dataEndpoint;
    }

    public String getMetadataEndpoint() {
        return metadataEndpoint;
    }

    public void setMetadataEndpoint(String metadataEndpoint) {
        this.metadataEndpoint = metadataEndpoint;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
