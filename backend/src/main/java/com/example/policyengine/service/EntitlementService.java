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

    public List<Entitlement> batchUpsert(List<Entitlement> entitlements) {
        return entitlements.stream().map(incoming -> {
            // Find existing entitlement by business key
            List<Entitlement> existingList = entitlementRepository.findByResourceTypeAndSubjectTypeAndSubjectId(
                    incoming.getResourceType(),
                    incoming.getSubjectType(),
                    incoming.getSubjectId());

            if (!existingList.isEmpty()) {
                // Update the first matching entitlement (assuming 1:1 mapping for
                // Subject+ResourceType for simplicity)
                // In a more complex scenario, we might merge, but "Upsert" usually implies
                // "Update specific target".
                // Since our model allows multiple rows (List return), we pick the first to
                // update or we'd need a better unique constraint.
                // Given the requirement is just "services want to push", updating the primary
                // record found is safest.
                Entitlement existing = existingList.get(0);
                existing.setResourceIds(incoming.getResourceIds()); // Overwrite resource IDs with the pushed state
                existing.setActions(incoming.getActions());
                existing.setEffect(incoming.getEffect());
                return entitlementRepository.save(existing);
            } else {
                // Create new
                return entitlementRepository.save(incoming);
            }
        }).toList();
    }
}
