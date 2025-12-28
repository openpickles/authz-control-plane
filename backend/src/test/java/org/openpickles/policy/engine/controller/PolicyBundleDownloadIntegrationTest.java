package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.model.PolicyBinding;
import org.openpickles.policy.engine.repository.PolicyBindingRepository;
import org.openpickles.policy.engine.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class PolicyBundleDownloadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyBindingRepository policyBindingRepository;

    @Autowired
    private org.openpickles.policy.engine.repository.PolicyBundleRepository policyBundleRepository;

    @BeforeEach
    public void setup() {
        // Setup existing data
        Policy policy = new Policy();
        policy.setName("test.policy");
        policy.setContent("package test.policy\ndefault allow = true");
        policy.setFilename("test.rego");
        policyRepository.save(policy);

        PolicyBinding binding = new PolicyBinding();
        binding.setResourceType("DOCUMENT");
        binding.setContext("HEADQUARTERS");
        binding.setPolicyIds(java.util.List.of(policy.getId()));
        binding.setEvaluationMode("OFFLINE");
        policyBindingRepository.save(binding);
        binding.setEvaluationMode("DIRECT");
        policyBindingRepository.save(binding);
    }

    @Test
    public void testDownloadBundle_ByResourceType_Success() throws Exception {
        mockMvc.perform(get("/api/v1/bundles/download")
                .with(user("admin").roles("ADMIN"))
                .param("resourceTypes", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gzip"))
                .andExpect(content().contentType("application/gzip"));
        // Further verification of tar content could be done by unzipping, but status
        // 200 + content type is a strong signal for now.
    }

    @Test
    public void testDownloadBundle_All_Success() throws Exception {
        mockMvc.perform(get("/api/v1/bundles/download") // No params
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gzip"));
    }

    @Test
    public void testDownloadBundle_Wasm_Success() throws Exception {
        // Create a WASM enabled bundle
        org.openpickles.policy.engine.model.PolicyBundle bundle = new org.openpickles.policy.engine.model.PolicyBundle();
        bundle.setName("wasm-bundle");
        bundle.setWasmEnabled(true);
        bundle.setEntrypoint("allow");

        PolicyBinding binding = policyBindingRepository.findAll().get(0);
        bundle.setBindingIds(java.util.List.of(binding.getId()));

        bundle = policyBundleRepository.save(bundle);

        try {
            mockMvc.perform(get("/api/v1/bundles/" + bundle.getId() + "/download")
                    .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/gzip"));
        } catch (Exception e) {
            // If OPA is missing or fails, we might get an exception.
            if (e.getCause() instanceof org.openpickles.policy.engine.exception.TechnicalException) {
                System.out.println("Test skipped or failed due to OPA execution error: " + e.getCause().getMessage());
                throw e;
            }
            throw e;
        }
    }

    @Test
    public void testCreateBundle_Wasm_Invalid_Entrypoint() throws Exception {
        // We need a binding with a policy to test validation
        PolicyBinding binding = policyBindingRepository.findAll().get(0);

        org.openpickles.policy.engine.model.PolicyBundle bundle = new org.openpickles.policy.engine.model.PolicyBundle();
        bundle.setName("invalid-wasm-bundle");
        bundle.setDescription("Testing invalid entrypoint");
        bundle.setWasmEnabled(true);
        bundle.setEntrypoint("non_existent_entrypoint/allow"); // This should fail if package doesn't match or rule
                                                               // doesn't exist
        bundle.setBindingIds(java.util.List.of(binding.getId()));

        String bundleJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bundle);

        mockMvc.perform(post("/api/v1/bundles")
                .with(user("admin").roles("ADMIN"))
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(bundleJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FUNC_WASM_INVALID"));
    }
}
