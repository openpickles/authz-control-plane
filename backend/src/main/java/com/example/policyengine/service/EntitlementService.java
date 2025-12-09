package com.example.policyengine.service;

import com.example.policyengine.model.Entitlement;
import com.example.policyengine.repository.EntitlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntitlementService {

    @Autowired
    private EntitlementRepository entitlementRepository;

    public List<Entitlement> getAllEntitlements() {
        return entitlementRepository.findAll();
    }

    public Entitlement createEntitlement(Entitlement entitlement) {
        return entitlementRepository.save(entitlement);
    }

    public Optional<Entitlement> getEntitlementById(Long id) {
        return entitlementRepository.findById(id);
    }

    public List<Entitlement> getEntitlementsByResource(String resourceType, String resourceId) {
        return entitlementRepository.findByResourceTypeAndResourceIdsContaining(resourceType, resourceId);
    }

    public List<Entitlement> getEntitlementsBySubject(Entitlement.SubjectType subjectType, String subjectId) {
        return entitlementRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId);
    }

    public Entitlement updateEntitlement(Long id, Entitlement entitlementDetails) {
        Entitlement entitlement = entitlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entitlement not found"));
        entitlement.setResourceType(entitlementDetails.getResourceType());
        entitlement.setResourceIds(entitlementDetails.getResourceIds());
        entitlement.setActions(entitlementDetails.getActions());
        entitlement.setSubjectType(entitlementDetails.getSubjectType());
        entitlement.setSubjectId(entitlementDetails.getSubjectId());
        entitlement.setEffect(entitlementDetails.getEffect());
        return entitlementRepository.save(entitlement);
    }

    public void deleteEntitlement(Long id) {
        entitlementRepository.deleteById(id);
    }
}
