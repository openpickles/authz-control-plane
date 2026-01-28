package org.openpickles.policy.engine.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policy_bundles")
public class PolicyBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "wasm_enabled")
    private boolean wasmEnabled = false;

    private String entrypoint = "allow";

    @ElementCollection
    @CollectionTable(name = "policy_bundle_bindings", joinColumns = @JoinColumn(name = "bundle_id"))
    @Column(name = "binding_id")
    private List<Long> bindingIds = new ArrayList<>();

    private String serviceOwner;

    // Added for Federated Model
    @Column(name = "target_service")
    private String targetService;

    @Column(name = "refresh_interval")
    private String refreshInterval;

    @ManyToMany(mappedBy = "subscribedBundles")
    @JsonIgnore
    private List<ServiceRegistry> subscribers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private org.openpickles.policy.engine.model.Policy.PolicyOrigin origin = org.openpickles.policy.engine.model.Policy.PolicyOrigin.CUSTOM;

    public PolicyBundle() {
    }

    public PolicyBundle(String name, String description, List<Long> bindingIds, boolean wasmEnabled,
            String entrypoint) {
        this.name = name;
        this.description = description;
        this.bindingIds = bindingIds;
        this.wasmEnabled = wasmEnabled;
        this.entrypoint = entrypoint;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getBindingIds() {
        return bindingIds;
    }

    public void setBindingIds(List<Long> bindingIds) {
        this.bindingIds = bindingIds;
    }

    public boolean isWasmEnabled() {
        return wasmEnabled;
    }

    public void setWasmEnabled(boolean wasmEnabled) {
        this.wasmEnabled = wasmEnabled;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    public String getServiceOwner() {
        return serviceOwner;
    }

    public void setServiceOwner(String serviceOwner) {
        this.serviceOwner = serviceOwner;
    }

    public org.openpickles.policy.engine.model.Policy.PolicyOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(org.openpickles.policy.engine.model.Policy.PolicyOrigin origin) {
        this.origin = origin;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public List<ServiceRegistry> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<ServiceRegistry> subscribers) {
        this.subscribers = subscribers;
    }
}
