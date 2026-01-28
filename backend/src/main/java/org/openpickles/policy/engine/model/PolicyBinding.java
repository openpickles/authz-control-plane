package org.openpickles.policy.engine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "policy_bindings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "resourceType", "context", "serviceOwner" })
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
    private java.util.List<Long> policyIds = new java.util.ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationMode evaluationMode; // DIRECT, RBAC_ONLY, PBC_CHAIN, ATTRIBUTE, CONDITION

    private String serviceOwner;

    public PolicyBinding() {
    }

    public PolicyBinding(String resourceType, String context, java.util.List<Long> policyIds,
            EvaluationMode evaluationMode) {
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

    public java.util.List<Long> getPolicyIds() {
        return policyIds;
    }

    public void setPolicyIds(java.util.List<Long> policyIds) {
        this.policyIds = policyIds;
    }

    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    public String getServiceOwner() {
        return serviceOwner;
    }

    public void setServiceOwner(String serviceOwner) {
        this.serviceOwner = serviceOwner;
    }
}
