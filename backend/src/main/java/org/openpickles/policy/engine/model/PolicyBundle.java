package org.openpickles.policy.engine.model;

import jakarta.persistence.*;
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
}
