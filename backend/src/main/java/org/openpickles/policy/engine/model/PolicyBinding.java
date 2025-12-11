package org.openpickles.policy.engine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "policy_bindings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "resourceType", "context" })
})
public class PolicyBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String context;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_binding_policies", joinColumns = @JoinColumn(name = "policy_binding_id"))
    @Column(name = "policy_id")
    private java.util.List<String> policyIds = new java.util.ArrayList<>();

    @Column(nullable = false)
    private String evaluationMode; // DIRECT, ATTRIBUTE, CONDITION

    public PolicyBinding() {
    }

    public PolicyBinding(String resourceType, String context, java.util.List<String> policyIds, String evaluationMode) {
        this.resourceType = resourceType;
        this.context = context;
        this.policyIds = policyIds;
        this.evaluationMode = evaluationMode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public java.util.List<String> getPolicyIds() {
        return policyIds;
    }

    public void setPolicyIds(java.util.List<String> policyIds) {
        this.policyIds = policyIds;
    }

    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }
}
