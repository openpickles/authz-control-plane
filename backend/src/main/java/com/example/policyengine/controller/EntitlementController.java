package com.example.policyengine.controller;

import com.example.policyengine.model.Entitlement;
import com.example.policyengine.service.EntitlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entitlements")
public class EntitlementController {

    @Autowired
    private EntitlementService entitlementService;

    @GetMapping
    public List<Entitlement> getAllEntitlements() {
        return entitlementService.getAllEntitlements();
    }

    @PostMapping
    public Entitlement createEntitlement(@RequestBody Entitlement entitlement) {
        return entitlementService.createEntitlement(entitlement);
    }

    @GetMapping("/resource")
    public List<Entitlement> getByResource(@RequestParam String type, @RequestParam String id) {
        return entitlementService.getEntitlementsByResource(type, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entitlement> getEntitlementById(@PathVariable Long id) {
        return entitlementService.getEntitlementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entitlement> updateEntitlement(@PathVariable Long id,
            @RequestBody Entitlement entitlementDetails) {
        return ResponseEntity.ok(entitlementService.updateEntitlement(id, entitlementDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntitlement(@PathVariable Long id) {
        entitlementService.deleteEntitlement(id);
        return ResponseEntity.ok().build();
    }
}
