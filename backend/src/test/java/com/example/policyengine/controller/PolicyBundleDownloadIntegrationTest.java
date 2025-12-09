package com.example.policyengine.controller;

import com.example.policyengine.model.Policy;
import com.example.policyengine.model.PolicyBinding;
import com.example.policyengine.repository.PolicyBindingRepository;
import com.example.policyengine.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        policyRepository.save(policy);
        policyRepository.save(policy);

        PolicyBinding binding = new PolicyBinding();
        binding.setPolicyId("test.policy");
        binding.setResourceType("DOCUMENT");
        binding.setContext("HEADQUARTERS");
        binding.setEvaluationMode("OFFLINE");
        policyBindingRepository.save(binding);
    }

    @Test
    @WithMockUser
    public void testDownloadBundle_ByResourceType_Success() throws Exception {
        mockMvc.perform(get("/api/v1/bundles/download")
                .param("resourceTypes", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gzip"))
                .andExpect(content().contentType("application/gzip"));
        // Further verification of tar content could be done by unzipping, but status
        // 200 + content type is a strong signal for now.
    }

    @Test
    @WithMockUser
    public void testDownloadBundle_All_Success() throws Exception {
        mockMvc.perform(get("/api/v1/bundles/download")) // No params
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gzip"));
    }
}
