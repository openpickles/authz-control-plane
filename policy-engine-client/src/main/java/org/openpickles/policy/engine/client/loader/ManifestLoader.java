package org.openpickles.policy.engine.client.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.openpickles.policy.engine.client.model.manifest.ClientManifest;
import org.openpickles.policy.engine.client.model.manifest.BindingDefinition;
import org.openpickles.policy.engine.client.model.manifest.PolicyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ManifestLoader {
    private static final Logger log = LoggerFactory.getLogger(ManifestLoader.class);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public ClientManifest loadManifest(String manifestPath) {
        try {
            if (manifestPath == null || manifestPath.isEmpty()) {
                manifestPath = "policy-manifest.yaml";
            }
            // Support "classpath:" prefix
            if (manifestPath.startsWith("classpath:")) {
                manifestPath = manifestPath.substring("classpath:".length());
            }

            ClassPathResource manifestResource = new ClassPathResource(manifestPath);
            if (!manifestResource.exists()) {
                log.info("No {} found in classpath. Skipping distributed sync.", manifestPath);
                return null;
            }

            try (InputStream is = manifestResource.getInputStream()) {
                ClientManifest manifest = yamlMapper.readValue(is, ClientManifest.class);
                loadPolicyContents(manifest);
                return manifest;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load policy manifest", e);
        }
    }

    private void loadPolicyContents(ClientManifest manifest) {
        if (manifest.getPolicies() == null)
            return;

        for (PolicyReference policy : manifest.getPolicies()) {
            if (policy.getFile() != null && policy.getContent() == null) {
                String content = loadPolicyFile(policy.getFile());
                policy.setContent(content);
            }
        }
    }

    private String loadPolicyFile(String filePath) {
        try {
            // Ensure path is relative to classpath root or policies dir?
            // Design doc says: "manifest file path should be relative to
            // src/main/resources/ (e.g., policies/auth.rego)"
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                // Try adding policies/ prefix if not present? No, stick to design "relative to
                // src/main/resources".
                throw new RuntimeException("Policy file not found: " + filePath);
            }
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read policy file: " + filePath, e);
        }
    }
}
