package org.openpickles.policy.engine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resource_providers")
public class ResourceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String fetchEndpoint;

    public ResourceProvider() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFetchEndpoint() {
        return fetchEndpoint;
    }

    public void setFetchEndpoint(String fetchEndpoint) {
        this.fetchEndpoint = fetchEndpoint;
    }
}
