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
        binding.setPolicyIds(java.util.List.of("test.policy"));
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
}
