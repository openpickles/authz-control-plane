package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.exception.TechnicalException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;

import java.util.Map;

@Service
public class EvaluationService {

    private final RestTemplate restTemplate;
    // OPA is running locally on port 8181
    private final String OPA_URL = "http://localhost:8181";

    public EvaluationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validates Rego syntax by attempting to compile it via OPA.
     * Note: OPA's Compile API is complex. A simpler way for "validation" is to try
     * "PUT" it to a temp path
     * or use the underlying library.
     * For now, we will try to PUT it to a temporary ID and check for 200 OK.
     */
    public void validatePolicy(String content) {
        // We use a temp policy ID for validation to avoid conflicts
        String tempId = "validation/temp";
        String url = OPA_URL + "/v1/policies/" + tempId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        // OPA expects raw text body for Policy PUT
        HttpEntity<String> request = new HttpEntity<>(content, headers);

        try {
            restTemplate.put(url, request);
        } catch (Exception e) {
            throw new TechnicalException("Policy validation failed: " + e.getMessage(), "TECH_OPA_VAL", e);
        }
    }

    /**
     * Tests a policy with input and data.
     * 1. If policyContent is provided (unsaved), push it to a temp location.
     * 2. Query OPA using the input.
     * 3. (Optional) mocking 'data' context is harder in OPA server mode compared to
     * 'opa eval'.
     * For Server mode, 'data' must be pushed via PUT /v1/data.
     * We will simulate 'data' by pushing it to a distinct path or expecting it to
     * be passed as 'input' wrapper if the policy supports it.
     * HOWEVER, for true "Mock Data" support in server mode, we should push to
     * /v1/data (replacing or merging).
     * This is risky in a shared env, but OK for this single-tenant embedded setup.
     */
    public Map<String, Object> testPolicy(String policyContent, String policyId, Map<String, Object> input,
            Map<String, Object> contextData) {
        // Step 1: Handle Context Data
        if (contextData != null && !contextData.isEmpty()) {
            pushContextData(contextData);
        }

        // Step 2: Handle Policy Content
        // If content is provided, we prefer that (Testing unsaved changes). Use a temp
        // ID.
        // If not, we assume necessary policies are already loaded (e.g. synced).
        if (policyContent != null && !policyContent.isEmpty()) {
            // Push temp policy
            pushTempPolicy(policyContent);
        }

        // Step 3: Evaluate
        // We assume the policy entry point is 'allow' or similar, but the user might
        // want to query arbitrary paths.
        // For simplicity, let's assume we query 'data' to see everything, OR the user
        // specifies the path in 'input' or separate param?
        // The Requirement was "Input and Data".
        // Let's assume a standard query path like "data.policy.allow" isn't generic
        // enough.
        // We will default to querying "data" which returns everything, or maybe a
        // specific package if we parse it.
        // Better: Query "data".

        String queryUrl = OPA_URL + "/v1/data";

        // request body for OPA data query: { "input": ... }
        Map<String, Object> requestBody = Map.of("input", input != null ? input : Map.of());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(queryUrl, requestBody, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new TechnicalException("Policy evaluation failed", "TECH_OPA_EVAL", e);
        }
    }

    private void pushContextData(Map<String, Object> data) {
        String url = OPA_URL + "/v1/data";
        try {
            restTemplate.put(url, data);
        } catch (Exception e) {
            throw new TechnicalException("Failed to push context data", "TECH_OPA_DATA", e);
        }
    }

    private void pushTempPolicy(String content) {
        String url = OPA_URL + "/v1/policies/temp_test_policy";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(content, headers);
        try {
            restTemplate.put(url, request);
        } catch (Exception e) {
            throw new TechnicalException("Failed to load temporary policy", "TECH_OPA_LOAD", e);
        }
    }
}
