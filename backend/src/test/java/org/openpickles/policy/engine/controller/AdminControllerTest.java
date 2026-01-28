package org.openpickles.policy.engine.controller;

import org.junit.jupiter.api.Test;
import org.openpickles.policy.engine.model.Policy;
import org.openpickles.policy.engine.repository.ServiceRegistryRepository;
import org.openpickles.policy.engine.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private ServiceRegistryRepository serviceRegistryRepository;

    @Test
    public void testCreateCustomPolicy_Success() throws Exception {
        Policy policy = new Policy();
        policy.setName("custom-policy");
        policy.setContent("package custom");

        mockMvc.perform(post("/api/v1/policies/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"custom-policy\", \"content\":\"package custom\"}"))
                .andExpect(status().isCreated());

        verify(policyService).createPolicy(any(Policy.class));
    }
}
