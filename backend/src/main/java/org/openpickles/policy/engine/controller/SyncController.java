package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.dto.request.ManifestSyncRequest;
import org.openpickles.policy.engine.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dist")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncManifest(@RequestBody ManifestSyncRequest request) {
        syncService.processManifest(request);
        return ResponseEntity.ok("Manifest processed successfully");
    }
}
