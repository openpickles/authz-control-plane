package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.model.Entitlement;
import org.openpickles.policy.engine.repository.EntitlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntitlementService {

    @Autowired
    private EntitlementRepository entitlementRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EntitlementService.class);

    public org.springframework.data.domain.Page<Entitlement> getAllEntitlements(
            org.springframework.data.domain.Pageable pageable, String search) {
        logger.debug("Fetching all entitlements, search: {}", search);
        if (search != null && !search.trim().isEmpty()) {
            return entitlementRepository.findBySubjectIdContainingIgnoreCaseOrResourceTypeContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        }
        return entitlementRepository.findAll(pageable);
    }

    public List<Entitlement> getAllEntitlements() {
        logger.debug("Fetching all entitlements (unpaged)");
        return entitlementRepository.findAll();
    }

    public Entitlement createEntitlement(Entitlement entitlement) {
        logger.info("Creating entitlement for subject: {}", entitlement.getSubjectId());
        return entitlementRepository.save(entitlement);
    }

    public Optional<Entitlement> getEntitlementById(Long id) {
        return entitlementRepository.findById(id);
    }

    public List<Entitlement> getEntitlementsByResource(String resourceType, String resourceId) {
        logger.debug("Fetching entitlements for resource: {}/{}", resourceType, resourceId);
        return entitlementRepository.findByResourceTypeAndResourceIdsContaining(resourceType, resourceId);
    }

    public List<Entitlement> getEntitlementsBySubject(Entitlement.SubjectType subjectType, String subjectId) {
        logger.debug("Fetching entitlements for subject: {}/{}", subjectType, subjectId);
        return entitlementRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId);
    }

    public Entitlement updateEntitlement(Long id, Entitlement entitlementDetails) {
        logger.info("Updating entitlement: {}", id);
        Entitlement entitlement = entitlementRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Entitlement not found with id: " + id, "FUNC_004"));

        entitlement.setResourceType(entitlementDetails.getResourceType());
        entitlement.setResourceIds(entitlementDetails.getResourceIds());
        entitlement.setActions(entitlementDetails.getActions());
        entitlement.setSubjectType(entitlementDetails.getSubjectType());
        entitlement.setSubjectId(entitlementDetails.getSubjectId());
        entitlement.setEffect(entitlementDetails.getEffect());
        return entitlementRepository.save(entitlement);
    }

    public void deleteEntitlement(Long id) {
        logger.info("Deleting entitlement: {}", id);
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
