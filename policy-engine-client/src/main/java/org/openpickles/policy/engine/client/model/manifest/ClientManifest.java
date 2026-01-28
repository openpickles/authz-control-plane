package org.openpickles.policy.engine.client.model.manifest;

import java.util.List;

public class ClientManifest {
    private String apiVersion;
    private ServiceInfo service;
    private List<ResourceTypeDefinition> resourceTypes;
    private List<PolicyReference> policies;
    private List<BindingDefinition> bindings;
    private List<BundleDefinition> bundles;

    // Getters and Setters
    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public ServiceInfo getService() {
        return service;
    }

    public void setService(ServiceInfo service) {
        this.service = service;
    }

    public List<ResourceTypeDefinition> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<ResourceTypeDefinition> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public List<PolicyReference> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PolicyReference> policies) {
        this.policies = policies;
    }

    public List<BindingDefinition> getBindings() {
        return bindings;
    }

    public void setBindings(List<BindingDefinition> bindings) {
        this.bindings = bindings;
    }

    public List<BundleDefinition> getBundles() {
        return bundles;
    }

    public void setBundles(List<BundleDefinition> bundles) {
        this.bundles = bundles;
    }
}
