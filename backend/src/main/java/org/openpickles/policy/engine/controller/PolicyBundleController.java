package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Entitlement;
import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.model.PolicyBinding;
import org.openpickles.policy.engine.model.PolicyBundle;
import org.openpickles.policy.engine.repository.EntitlementRepository;
import org.openpickles.policy.engine.repository.PolicyBindingRepository;
import org.openpickles.policy.engine.repository.PolicyBundleRepository;
import org.openpickles.policy.engine.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bundles")
public class PolicyBundleController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PolicyBundleController.class);

    @Autowired
    private PolicyBundleRepository bundleRepository;

    @Autowired
    private PolicyBindingRepository bindingRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private EntitlementRepository entitlementRepository;

    @GetMapping
    public List<PolicyBundle> getAllBundles() {
        logger.debug("Fetching all bundles");
        return bundleRepository.findAll();
    }

    @PostMapping
    public PolicyBundle createBundle(@RequestBody PolicyBundle bundle) {
        logger.info("Creating bundle: {}", bundle.getName());
        return bundleRepository.save(bundle);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadBundles(@RequestParam(required = false) List<String> resourceTypes) {
        logger.info("Downloading dynamic bundle for resourceTypes: {}", resourceTypes);
        List<PolicyBinding> bindings;
        if (resourceTypes == null || resourceTypes.isEmpty()) {
            bindings = bindingRepository.findAll();
        } else {
            bindings = bindingRepository.findByResourceTypeIn(resourceTypes);
        }
        return generateBundleResponse(bindings, "bundle-" + Instant.now().toEpochMilli());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadBundle(@PathVariable Long id) {
        logger.info("Downloading bundle by id: {}", id);
        return bundleRepository.findById(id)
                .map(bundle -> {
                    List<PolicyBinding> bindings = bindingRepository.findAllById(bundle.getBindingIds());
                    return generateBundleResponse(bindings, "bundle-" + id);
                })
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Bundle not found with id: " + id, "FUNC_003"));
    }

    private ResponseEntity<byte[]> generateBundleResponse(List<PolicyBinding> bindings, String filenameBase) {
        try {
            // 1. Fetch Data
            Set<String> policyIds = bindings.stream()
                    .flatMap(b -> b.getPolicyIds().stream())
                    .collect(Collectors.toSet());
            List<Policy> policies = policyRepository.findByNameIn(policyIds);

            Set<String> resourceTypes = bindings.stream()
                    .map(PolicyBinding::getResourceType)
                    .collect(Collectors.toSet());
            List<Entitlement> allEntitlements = entitlementRepository.findAll();
            List<Entitlement> filteredEntitlements = allEntitlements.stream()
                    .filter(e -> resourceTypes.contains(e.getResourceType()))
                    .collect(Collectors.toList());

            // 2. Prepare Data JSON
            Map<String, Object> dataJson = new HashMap<>();
            dataJson.put("bindings", bindings);
            dataJson.put("entitlements", filteredEntitlements);

            // 3. Create Tar GZ
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (org.apache.commons.compress.archivers.tar.TarArchiveOutputStream tarOut = new org.apache.commons.compress.archivers.tar.TarArchiveOutputStream(
                    new java.util.zip.GZIPOutputStream(baos))) {

                // Add data.json
                String jsonContent = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(dataJson);
                org.apache.commons.compress.archivers.tar.TarArchiveEntry dataEntry = new org.apache.commons.compress.archivers.tar.TarArchiveEntry(
                        "data.json");
                dataEntry.setSize(jsonContent.getBytes().length);
                tarOut.putArchiveEntry(dataEntry);
                tarOut.write(jsonContent.getBytes());
                tarOut.closeArchiveEntry();

                // Add Policies
                for (Policy policy : policies) {
                    String policyContent = policy.getContent();
                    if (policyContent == null)
                        policyContent = "";
                    String filename = "policies/"
                            + (policy.getFilename() != null ? policy.getFilename() : policy.getName() + ".rego");
                    org.apache.commons.compress.archivers.tar.TarArchiveEntry policyEntry = new org.apache.commons.compress.archivers.tar.TarArchiveEntry(
                            filename);
                    policyEntry.setSize(policyContent.getBytes().length);
                    tarOut.putArchiveEntry(policyEntry);
                    tarOut.write(policyContent.getBytes());
                    tarOut.closeArchiveEntry();
                }

                tarOut.finish();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filenameBase + ".tar.gz\"")
                    .contentType(MediaType.parseMediaType("application/gzip"))
                    .body(baos.toByteArray());

        } catch (Exception e) {
            logger.error("Failed to generate bundle", e);
            throw new org.openpickles.policy.engine.exception.TechnicalException("Error generating bundle", "TECH_001",
                    e);
        }
    }
}
