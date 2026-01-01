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

    @GetMapping
    public org.springframework.data.domain.Page<PolicyBundle> getAllBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        logger.debug("Fetching bundles, page: {}, size: {}, search: {}", page, size, search);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (search != null && !search.trim().isEmpty()) {
            return bundleRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return bundleRepository.findAll(pageable);
    }

    @PostMapping
    public PolicyBundle createBundle(@RequestBody PolicyBundle bundle) {
        logger.info("Creating bundle: {}", bundle.getName());
        if (bundle.isWasmEnabled()) {
            validateWasmBundle(bundle);
        }
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
        return generateBundleResponse(bindings, "bundle-" + Instant.now().toEpochMilli(), false, "allow");
    }

    @GetMapping("/{id}/download")
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

    private ResponseEntity<byte[]> generateBundleResponse(List<PolicyBinding> bindings, String filenameBase,
            boolean wasmEnabled, String entrypoint) {
        Path tempDir = null;
        try {
            // 1. Fetch Data
            Set<Long> policyIds = bindings.stream()
                    .flatMap(b -> b.getPolicyIds().stream())
                    .collect(Collectors.toSet());
            List<Policy> policies = policyRepository.findAllById(policyIds);

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
                        throw new SecurityException("Invalid entrypoint format");
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
