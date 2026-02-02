package org.openpickles.policy.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openpickles.policy.engine.dto.request.ManifestSyncRequest;
import org.openpickles.policy.engine.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SyncController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
public class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncService syncService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSyncManifest_Success() throws Exception {
        ManifestSyncRequest request = new ManifestSyncRequest();
        org.openpickles.policy.engine.dto.manifest.PolicyManifest manifest = new org.openpickles.policy.engine.dto.manifest.PolicyManifest();
        org.openpickles.policy.engine.dto.manifest.ServiceInfo serviceInfo = new org.openpickles.policy.engine.dto.manifest.ServiceInfo();
        serviceInfo.setName("test-service");
        manifest.setService(serviceInfo);
        request.setManifest(manifest);
        request.setManifestHash("hash123");

        mockMvc.perform(post("/api/v1/dist/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(syncService).processManifest(any(ManifestSyncRequest.class));
    }

    @TestConfiguration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
