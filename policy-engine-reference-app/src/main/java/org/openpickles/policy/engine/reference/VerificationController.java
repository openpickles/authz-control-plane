package org.openpickles.policy.engine.reference;

import org.openpickles.policy.engine.client.ClientConfig;
import org.openpickles.policy.engine.client.PolicyEngineClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class VerificationController {

    // Simulating "Enforcement Logic" state
    // In a real app, the client pushes to OPA. Here we might just track what we
    // received.
    // Since PolicyEngineClient in this version pushes to OPA or does internal
    // logic,
    // we need to see how we can inspect the state.

    // Looking at PolicyEngineClient.java, it logs receipt.
    // It doesn't seem to expose "currentVersion" publicly.
    // For the purpose of this driver, we might need to rely on logs or mock the OPA
    // endpoint?

    // Wait! The PolicyEngineClient *pushes* to OPA URL if configured.
    // We can configure the "OPA URL" to be *this application*!
    // So the client will download the bundle and PUT it to
    // localhost:port/v1/policies/bundle
    // We can intercept that!

    private String lastReceivedBundleVersion;
    private byte[] lastReceivedBundleContent;

    @GetMapping("/verification/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("lastReceivedBundleVersion", lastReceivedBundleVersion); // We don't have version in content yet
                                                                            // usually, but we can infer or store
                                                                            // metadata
        status.put("receivedContentSize", lastReceivedBundleContent != null ? lastReceivedBundleContent.length : 0);
        return status;
    }

    // Mock OPA Endpoint
    @org.springframework.web.bind.annotation.PutMapping("/v1/policies/{policyId}")
    public void receiveBundleMOCKED(@org.springframework.web.bind.annotation.PathVariable String policyId,
            @org.springframework.web.bind.annotation.RequestBody byte[] content) {
        System.out.println("MOCKED OPA: Received bundle for " + policyId);
        this.lastReceivedBundleContent = content;
        this.lastReceivedBundleVersion = "LATEST"; // Simplified
    }
}
