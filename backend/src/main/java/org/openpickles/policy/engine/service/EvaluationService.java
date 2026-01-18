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

    private final org.openpickles.policy.engine.repository.PolicyBindingRepository policyBindingRepository;
    private final org.openpickles.policy.engine.repository.PolicyRepository policyRepository;

    public EvaluationService(org.openpickles.policy.engine.repository.PolicyBindingRepository policyBindingRepository,
            org.openpickles.policy.engine.repository.PolicyRepository policyRepository) {
        this.policyBindingRepository = policyBindingRepository;
        this.policyRepository = policyRepository;
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

    /**
     * Main API for Policy Evaluation based on Bindings.
     * 
     * @param resourceType Service/Resource Type (e.g. "payment-service")
     * @param context      Context (e.g. "transaction-write")
     * @param input        Access Request Input (e.g. { "user": "..." })
     * @return Evaluation Result
     */
    public Map<String, Object> evaluate(String resourceType, String context, Map<String, Object> input) {
        java.util.Optional<org.openpickles.policy.engine.model.PolicyBinding> bindingOpt = policyBindingRepository
                .findByResourceTypeAndContext(resourceType, context);

        if (bindingOpt.isEmpty()) {
            // Default Deny if no binding found
            return Map.of("allowed", false, "reason", "No binding found");
        }

        org.openpickles.policy.engine.model.PolicyBinding binding = bindingOpt.get();

        switch (binding.getEvaluationMode()) {
            case DIRECT:
                return evaluateDirect(binding, input);
            case RBAC_ONLY:
                return evaluateRbac(input);
            case PBC_CHAIN:
            case ATTRIBUTE:
            case CONDITION:
                return Map.of("allowed", false, "reason", "Not implemented yet");
            default:
                return Map.of("allowed", false, "reason", "Unknown mode");
        }
    }

    private Map<String, Object> evaluateRbac(Map<String, Object> input) {
        // Simple mock checks "role" in input. In reality, would query RBAC
        // policy/service
        if (input != null && input.containsKey("role") && "ADMIN".equals(input.get("role"))) {
            return Map.of("allowed", true);
        }
        return Map.of("allowed", false, "reason", "RBAC Deny");
    }

    private Map<String, Object> evaluateDirect(org.openpickles.policy.engine.model.PolicyBinding binding,
            Map<String, Object> input) {
        java.util.List<org.openpickles.policy.engine.model.Policy> policies = policyRepository
                .findAllById(binding.getPolicyIds());

        if (policies.isEmpty()) {
            return Map.of("allowed", false, "reason", "No policies in binding");
        }

        for (org.openpickles.policy.engine.model.Policy policy : policies) {
            // We assume policy package is named 'policy.<id>' for now to be distinct, or
            // check 'allow' in global data
            // Since we don't enforce package naming yet, we will query 'data' and inspect.

            // Note: In a real system, we'd know exactly which package to query.
            // Here, we'll optimistically query "data" with the input and look for any
            // "allow": true at top level or in packages.

            Map<String, Object> result = testPolicy(null, null, input, null);
            // We reuse testPolicy but we don't push content (it should be there).
            // But wait, if testPolicy returns the raw OPA result...

            if (isAllowed(result)) {
                return Map.of("allowed", true, "policy", policy.getName());
            }
        }

        return Map.of("allowed", false, "reason", "No policy allowed access");
    }

    // Helper to parse OPA response { "result": { ... } }
    private boolean isAllowed(Map<String, Object> opaResponse) {
        if (opaResponse == null || !opaResponse.containsKey("result"))
            return false;

        Object res = opaResponse.get("result");
        if (res instanceof Map) {
            Map<?, ?> resMap = (Map<?, ?>) res;
            // Check generic "allow"
            if (resMap.containsKey("allow") && Boolean.TRUE.equals(resMap.get("allow")))
                return true;

            // Check nested packages?
            // This is heuristic.
        }
        return false;
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
