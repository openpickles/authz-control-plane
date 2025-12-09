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

    @Column(nullable = false)
    private String policyId;

    @Column(nullable = false)
    private String evaluationMode; // DIRECT, ATTRIBUTE, CONDITION

    public PolicyBinding() {
    }

    public PolicyBinding(String resourceType, String context, String policyId, String evaluationMode) {
        this.resourceType = resourceType;
        this.context = context;
        this.policyId = policyId;
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

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }
}
