package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Entitlement;
import org.openpickles.policy.engine.service.EntitlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entitlements")
public class EntitlementController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EntitlementController.class);

    @Autowired
    private EntitlementService entitlementService;

    @GetMapping
    public org.springframework.data.domain.Page<Entitlement> getAllEntitlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        logger.debug("Request to get all entitlements, page: {}, size: {}, search: {}", page, size, search);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return entitlementService.getAllEntitlements(pageable, search);
    }

    @PostMapping
    public Entitlement createEntitlement(@RequestBody Entitlement entitlement) {
        logger.info("Request to create entitlement for subject: {}", entitlement.getSubjectId());
        return entitlementService.createEntitlement(entitlement);
    }

    @PostMapping("/sync")
    public List<Entitlement> syncEntitlements(@RequestBody List<Entitlement> entitlements) {
        logger.info("Request to sync {} entitlements", entitlements.size());
        return entitlementService.batchUpsert(entitlements);
    }

    @GetMapping("/resource")
    public List<Entitlement> getByResource(@RequestParam String type, @RequestParam String id) {
        logger.debug("Request to get entitlements for resource: {}/{}", type, id);
        return entitlementService.getEntitlementsByResource(type, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entitlement> getEntitlementById(@PathVariable Long id) {
        logger.debug("Request to get entitlement by id: {}", id);
        return entitlementService.getEntitlementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entitlement> updateEntitlement(@PathVariable Long id,
            @RequestBody Entitlement entitlementDetails) {
        logger.info("Request to update entitlement: {}", id);
        return ResponseEntity.ok(entitlementService.updateEntitlement(id, entitlementDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntitlement(@PathVariable Long id) {
        logger.info("Request to delete entitlement: {}", id);
        entitlementService.deleteEntitlement(id);
        return ResponseEntity.ok().build();
    }
}
