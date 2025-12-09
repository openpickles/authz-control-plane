package com.example.policyengine.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "entitlements")
public class Entitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceType;

    @ElementCollection
    @CollectionTable(name = "entitlement_resource_ids", joinColumns = @JoinColumn(name = "entitlement_id"))
    @Column(name = "resource_id")
    private Set<String> resourceIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "entitlement_actions", joinColumns = @JoinColumn(name = "entitlement_id"))
    @Column(name = "action")
    private Set<String> actions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private SubjectType subjectType;

    private String subjectId; // Could be username, role name, or group name

    @Enumerated(EnumType.STRING)
    private Effect effect;

    public enum SubjectType {
        USER, ROLE, GROUP
    }

    public enum Effect {
        ALLOW, DENY
    }

    public Entitlement() {
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

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }
}
