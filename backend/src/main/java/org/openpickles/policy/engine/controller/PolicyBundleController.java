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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.tomcat.util.http.fileupload.FileUtils;

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

    @Autowired
    private org.openpickles.policy.engine.repository.ResourceTypeRepository resourceTypeRepository;

    @Autowired(required = false)
    private org.openpickles.policy.engine.event.EventPublisher eventPublisher;

    @GetMapping
    public org.springframework.data.domain.Page<PolicyBundle> getAllBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String service) {
        logger.debug("Fetching bundles, page: {}, size: {}, search: {}, service: {}", page, size, search, service);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        if (service != null && !service.isEmpty()) {
            // New logic: Return ALL subscribed bundles for the service (which includes
            // owned ones)
            java.util.Optional<org.openpickles.policy.engine.model.ServiceRegistry> serviceRegOpt = serviceRegistryRepository
                    .findByName(service);

            if (serviceRegOpt.isPresent()) {
                Set<PolicyBundle> bundles = serviceRegOpt.get().getSubscribedBundles();

                // Filter by search query if present
                List<PolicyBundle> filtered = bundles.stream()
                        .filter(b -> search == null || search.trim().isEmpty()
                                || b.getName().toLowerCase().contains(search.trim().toLowerCase()))
                        .sorted(Comparator.comparing(PolicyBundle::getName)) // Sort specifically for consistent paging
                        .collect(Collectors.toList());

                // Paging in memory
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filtered.size());

                List<PolicyBundle> pagedList;
                if (start > filtered.size()) {
                    pagedList = new ArrayList<>();
                } else {
                    pagedList = filtered.subList(start, end);
                }

                return new org.springframework.data.domain.PageImpl<>(pagedList, pageable, filtered.size());
            } else {
                return org.springframework.data.domain.Page.empty();
            }
        }

        // Default: Find all global bundles (or just all bundles if no service
        // specified)
        if (search == null || search.trim().isEmpty()) {
            return bundleRepository.findAll(pageable);
        }

        org.openpickles.policy.engine.model.PolicyBundle probe = new org.openpickles.policy.engine.model.PolicyBundle();
        probe.setName(search.trim());

        org.springframework.data.domain.ExampleMatcher matcher = org.springframework.data.domain.ExampleMatcher
                .matching()
                .withIgnoreNullValues()
                .withIgnorePaths("wasmEnabled", "entrypoint", "origin", "id") // Ignore fields with default values
                .withStringMatcher(org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING);

        return bundleRepository.findAll(org.springframework.data.domain.Example.of(probe, matcher), pageable);
    }

    @Autowired
    private org.openpickles.policy.engine.repository.ServiceRegistryRepository serviceRegistryRepository;

    @PostMapping
    public PolicyBundle createBundle(@RequestBody PolicyBundle bundle) {
        logger.info("Creating bundle: {}", bundle.getName());

        // Validate Service Owner
        if (bundle.getServiceOwner() == null || bundle.getServiceOwner().isEmpty()) {
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Service Owner is required for bundle creation", "FUNC_BUNDLE_NO_SERVICE");
        }

        // Verify service exists (Optional but recommended)
        if (serviceRegistryRepository.findByName(bundle.getServiceOwner()).isEmpty()) {
            // For strict mode, we might want to fail. But legacy custom bundles might not
            // have service registered?
            // Requirement says "all these will be associated to a service".
            // So we should enforce it.
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "Service not registered: " + bundle.getServiceOwner(), "FUNC_Vk_SERVICE_NOT_FOUND");
        }

        if (bundle.isWasmEnabled()) {
            validateWasmBundle(bundle);
        }
        return bundleRepository.save(bundle);
    }

    @PostMapping("/{name}/build")
    public ResponseEntity<String> buildBundle(@PathVariable String name) {
        logger.info("Triggering build (and notification) for bundle: {}", name);
        PolicyBundle bundle = bundleRepository.findByName(name)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Bundle not found with name: " + name, "FUNC_004"));

        if (eventPublisher != null) {
            try {
                String version = java.util.UUID.randomUUID().toString();
                // Assuming the backend URL is localhost:8080 for this demo context
                // In production, this should be configurable
                String downloadUrl = "http://localhost:8080/api/v1/bundles/" + bundle.getId() + "/download";

                Map<String, Object> data = new HashMap<>();
                data.put("bundleName", bundle.getName());
                data.put("version", version);
                data.put("downloadUrl", downloadUrl);

                byte[] jsonBytes = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(data);

                io.cloudevents.CloudEvent event = io.cloudevents.core.builder.CloudEventBuilder.v1()
                        .withId(java.util.UUID.randomUUID().toString())
                        .withSource(java.net.URI.create("policy-engine"))
                        .withType("org.openpickles.policy.bundle.update")
                        .withData("application/json", jsonBytes)
                        .build();

                eventPublisher.publish("bundles/" + name, event);
                return ResponseEntity.ok("Build triggered and notification sent. Version: " + version);
            } catch (Exception e) {
                logger.error("Failed to publish notification", e);
                return ResponseEntity.status(500).body("Build triggered but notification failed");
            }
        } else {
            logger.warn("EventPublisher is not available. Skipping notification.");
            return ResponseEntity.ok("Build triggered (No notification sent - EventPublisher missing)");
        }
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
        return generateBundleResponse(bindings, "bundle-" + Instant.now().toEpochMilli(), false, "allow");
    }

    @GetMapping("/{id:[0-9]+}/download")
    public ResponseEntity<byte[]> downloadBundle(@PathVariable Long id) {
        logger.info("Downloading bundle by id: {}", id);
        return bundleRepository.findById(id)
                .map(bundle -> {
                    List<PolicyBinding> bindings = bindingRepository.findAllById(bundle.getBindingIds());
                    return generateBundleResponse(bindings, "bundle-" + id, bundle.isWasmEnabled(),
                            bundle.getEntrypoint());
                })
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Bundle not found with id: " + id, "FUNC_003"));
    }

    @GetMapping("/{name}/download")
    public ResponseEntity<byte[]> downloadBundleByName(
            @PathVariable String name,
            @RequestParam(required = false) String service) {
        logger.info("Downloading bundle by name: {} for service: {}", name, service);

        // Find bundle by name (checking both serviceOwner and global visibility logic
        // if we had it)
        // For now, we assume if we can find it by name, we start there.
        // But in the new model, we want the "Effective Bundle" for the service.

        List<PolicyBinding> effectiveBindings = new ArrayList<>();
        boolean wasmEnabled = false;
        String entrypoint = "allow";
        String filenameBase = "bundle-" + name;

        if (service != null && !service.isEmpty()) {
            java.util.Optional<org.openpickles.policy.engine.model.ServiceRegistry> serviceRegOpt = serviceRegistryRepository
                    .findByName(service);

            if (serviceRegOpt.isPresent()) {
                org.openpickles.policy.engine.model.ServiceRegistry serviceReg = serviceRegOpt.get();
                Set<PolicyBundle> subscriptions = serviceReg.getSubscribedBundles();

                // If the requested bundle is NOT in subscriptions, we should try to find it
                // specifically
                // (Compatibility for existing behavior or ad-hoc requests)
                // But generally, the 'name' param acts as the primary identifier.

                // Merge all subscribed bundles
                for (PolicyBundle b : subscriptions) {
                    List<PolicyBinding> bundleBindings = bindingRepository.findAllById(b.getBindingIds());
                    effectiveBindings.addAll(bundleBindings);

                    // Take WASM/Entrypoint settings from the *requested* bundle if it matches
                    // or just uses the first one that has them enabled?
                    // Strategy: The requested bundle (name) is the "Primary" one.
                    if (b.getName().equals(name)) {
                        wasmEnabled = b.isWasmEnabled();
                        entrypoint = b.getEntrypoint();
                    }
                }

                // If effectiveBindings is empty, maybe the requested bundle exists but is not
                // subscribed?
                // This happens during first run or if 'name' is just a pointer.
                // Let's fallback to strict single bundle lookup if Merge resulted in nothing
                // relevant to 'name'?
                // Or just proceed.
            } else {
                // Service not found? Fallback to legacy single bundle lookup
                return downloadSingleBundleLegacy(name, service);
            }
        } else {
            // No service param? Just download the named bundle alone
            return downloadSingleBundleLegacy(name, null);
        }

        // Deduplicate bindings (by ID)
        List<PolicyBinding> distinctBindings = effectiveBindings.stream()
                .distinct()
                .collect(Collectors.toList());

        // Also fetch policies for overlay logic
        Set<Long> policyIds = distinctBindings.stream()
                .flatMap(b -> b.getPolicyIds().stream())
                .collect(Collectors.toSet());
        List<Policy> productPolicies = policyRepository.findAllById(policyIds);

        // Apply Custom Overlays if service is present
        List<Policy> effectivePolicies = new ArrayList<>();
        for (Policy productPolicy : productPolicies) {
            if (service != null) {
                Optional<Policy> customPolicy = policyRepository.findByNameAndServiceOwnerAndOrigin(
                        productPolicy.getName(), service, Policy.PolicyOrigin.CUSTOM);
                effectivePolicies.add(customPolicy.orElse(productPolicy));
            } else {
                effectivePolicies.add(productPolicy);
            }
        }

        return generateBundleResponseWithPolicies(distinctBindings, effectivePolicies, filenameBase, wasmEnabled,
                entrypoint);
    }

    private ResponseEntity<byte[]> downloadSingleBundleLegacy(String name, String service) {
        PolicyBundle bundle = bundleRepository.findByName(name)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "Bundle not found with name: " + name, "FUNC_004"));

        List<PolicyBinding> bindings = bindingRepository.findAllById(bundle.getBindingIds());

        // Apply Overlay Logic
        List<Policy> effectivePolicies = new ArrayList<>();
        Set<Long> policyIds = bindings.stream()
                .flatMap(b -> b.getPolicyIds().stream())
                .collect(Collectors.toSet());
        List<Policy> productPolicies = policyRepository.findAllById(policyIds);

        for (Policy productPolicy : productPolicies) {
            if (service != null) {
                Optional<Policy> customPolicy = policyRepository.findByNameAndServiceOwnerAndOrigin(
                        productPolicy.getName(), service, Policy.PolicyOrigin.CUSTOM);
                effectivePolicies.add(customPolicy.orElse(productPolicy));
            } else {
                effectivePolicies.add(productPolicy);
            }
        }

        return generateBundleResponseWithPolicies(bindings, effectivePolicies, "bundle-" + name, bundle.isWasmEnabled(),
                bundle.getEntrypoint());
    }

    private ResponseEntity<byte[]> generateBundleResponse(List<PolicyBinding> bindings, String filenameBase,
            boolean wasmEnabled, String entrypoint) {
        Set<Long> policyIds = bindings.stream()
                .flatMap(b -> b.getPolicyIds().stream())
                .collect(Collectors.toSet());
        List<Policy> policies = policyRepository.findAllById(policyIds);
        return generateBundleResponseWithPolicies(bindings, policies, filenameBase, wasmEnabled, entrypoint);
    }

    private ResponseEntity<byte[]> generateBundleResponseWithPolicies(List<PolicyBinding> bindings,
            List<Policy> policies, String filenameBase,
            boolean wasmEnabled, String entrypoint) {
        Path tempDir = null;
        try {
            // 1. Fetch Data
            Set<String> resourceTypeKeys = bindings.stream()
                    .map(PolicyBinding::getResourceType)
                    .collect(Collectors.toSet());
            List<Entitlement> allEntitlements = entitlementRepository.findAll();
            List<Entitlement> filteredEntitlements = allEntitlements.stream()
                    .filter(e -> resourceTypeKeys.contains(e.getResourceType()))
                    .collect(Collectors.toList());

            // 1.1 Fetch Resource Types
            // We want to include the definitions for the resource types used in the
            // bindings
            // This allows the policy to access metadata (like PII flags) defined in the
            // schema
            List<org.openpickles.policy.engine.model.ResourceType> allResourceTypes = resourceTypeRepository.findAll();
            List<org.openpickles.policy.engine.model.ResourceType> filteredResourceTypes = allResourceTypes.stream()
                    .filter(rt -> resourceTypeKeys.contains(rt.getKey()))
                    .collect(Collectors.toList());

            // 2. Prepare Data JSON
            Map<String, Object> dataJson = new HashMap<>();
            dataJson.put("bindings", bindings);
            dataJson.put("entitlements", filteredEntitlements);
            dataJson.put("resource_types", filteredResourceTypes); // Inject Resource Types
            String jsonContent = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dataJson);

            if (wasmEnabled) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".tar.gz\"")
                        .contentType(MediaType.parseMediaType("application/gzip"))
                        .body(compileToWasm(policies, jsonContent, entrypoint));

            } else {
                // Legacy / Standard Bundle Construction
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try (org.apache.commons.compress.archivers.tar.TarArchiveOutputStream tarOut = new org.apache.commons.compress.archivers.tar.TarArchiveOutputStream(
                        new java.util.zip.GZIPOutputStream(baos))) {

                    // Add data.json
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
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameBase + ".tar.gz\"")
                        .contentType(MediaType.parseMediaType("application/gzip"))
                        .body(baos.toByteArray());
            }

        } catch (Exception e) {
            logger.error("Failed to generate bundle", e);
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "Error generating bundle: " + e.getMessage(), "TECH_001", e);
        } finally {
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void validateWasmBundle(PolicyBundle bundle) {
        try {
            // Fetch policies to validate them
            List<PolicyBinding> bindings = bindingRepository.findAllById(bundle.getBindingIds());
            if (bindings.isEmpty() && bundle.getBindingIds().isEmpty()) {
                return;
            }

            Set<Long> policyIds = bindings.stream()
                    .flatMap(b -> b.getPolicyIds().stream())
                    .collect(Collectors.toSet());
            List<Policy> policies = policyRepository.findAllById(policyIds);

            // Dummy data.json for validation
            String dummyJson = "{}";

            // Attempt compile
            compileToWasm(policies, dummyJson, bundle.getEntrypoint());

        } catch (org.openpickles.policy.engine.exception.TechnicalException e) {
            // Log full error
            logger.error("WASM Validation failed: {}", e.getMessage());
            // Throw functional error to user
            throw new org.openpickles.policy.engine.exception.FunctionalException(
                    "WASM Compilation Validation Failed: " + e.getMessage(), "FUNC_WASM_INVALID");
        } catch (Exception e) {
            logger.error("Unexpected error during WASM validation", e);
            throw new org.openpickles.policy.engine.exception.TechnicalException("Validation Error", "TECH_VAL_ERR", e);
        }
    }

    private byte[] compileToWasm(List<Policy> policies, String jsonContent, String entrypoint) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("opa-build");

            Files.write(tempDir.resolve("data.json"), jsonContent.getBytes(), StandardOpenOption.CREATE);

            List<String> entrypoints = preparePolicyFiles(policies, tempDir, entrypoint);

            if (entrypoints.isEmpty()) {
                logger.warn("No package names found in policies. WASM build might fail.");
            }

            return executeOpaBuild(tempDir, entrypoints);

        } catch (org.openpickles.policy.engine.exception.TechnicalException te) {
            throw te;
        } catch (Exception e) {
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "Error generating WASM bundle: " + e.getMessage(), "TECH_001", e);
        } finally {
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (Exception ignore) {
                }
            }
        }
    }

    private List<String> preparePolicyFiles(List<Policy> policies, Path tempDir, String entrypoint)
            throws java.io.IOException {
        List<String> entrypoints = new ArrayList<>();
        for (Policy policy : policies) {
            String content = policy.getContent() != null ? policy.getContent() : "";

            // Sanitize filename to prevent path traversal
            String rawFilename = policy.getFilename() != null ? policy.getFilename()
                    : "policy-" + policy.getId() + ".rego";
            String safeFilename = java.nio.file.Paths.get(rawFilename).getFileName().toString();

            Path policyPath = tempDir.resolve(safeFilename).normalize();
            if (!policyPath.startsWith(tempDir)) {
                throw new SecurityException("Path traversal attempt detected: " + rawFilename);
            }

            Files.write(policyPath, content.getBytes(), StandardOpenOption.CREATE);

            String packageName = findPackageName(content);
            if (packageName != null) {
                // Validate entrypoint if provided, otherwise default to 'allow'
                String epName = "allow";
                if (entrypoint != null && !entrypoint.isEmpty()) {
                    if (!entrypoint.matches("^\\w+$")) {
                        logger.error("Invalid entrypoint format: '{}'", entrypoint);
                        throw new SecurityException("Invalid entrypoint format: " + entrypoint);
                    }
                    epName = entrypoint;
                }
                entrypoints.add(packageName.replace(".", "/") + "/" + epName);
            }
        }
        return entrypoints;
    }

    private byte[] executeOpaBuild(Path tempDir, List<String> entrypoints)
            throws java.io.IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("opa");
        command.add("build");
        command.add("-t");
        command.add("wasm");
        command.add("-o");
        command.add("bundle.tar.gz");
        command.add("-b");
        command.add(".");
        for (String ep : entrypoints) {
            command.add("-e");
            command.add(ep);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("OPA Output: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.error("OPA Build Failed. Output:\n{}", output);
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "OPA build failed with exit code " + exitCode + ". Output: " + output.toString(),
                    "TECH_OPA_FAIL");
        }

        Path bundlePath = tempDir.resolve("bundle.tar.gz");
        if (!Files.exists(bundlePath)) {
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "OPA build succeeded but bundle.tar.gz not found", "TECH_OPA_NO_OUTPUT");
        }
        return Files.readAllBytes(bundlePath);
    }

    private String findPackageName(String content) {
        // Simple regex to find package declaration
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)",
                java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
