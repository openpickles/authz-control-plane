package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.PolicyBundle;
import org.openpickles.policy.engine.repository.PolicyBundleRepository;
import org.openpickles.policy.engine.repository.ServiceRegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class PolicyBundleControllerTest {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String API_BUNDLES = "/api/v1/bundles";
    private static final String SERVICE_NAME = "my-service";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyBundleRepository bundleRepository;

    @Autowired
    private ServiceRegistryRepository serviceRegistryRepository;

    @BeforeEach
    public void setup() {
        // Setup service
        org.openpickles.policy.engine.model.ServiceRegistry testService = new org.openpickles.policy.engine.model.ServiceRegistry();
        testService.setName(SERVICE_NAME);
        testService.setVersion("1.0.0");
        testService.setRegistrationMode(org.openpickles.policy.engine.model.ServiceRegistry.RegistrationMode.MANUAL);
        serviceRegistryRepository.save(testService);
    }

    @Test
    public void testCreateBundleSuccess() throws Exception {
        PolicyBundle bundle = new PolicyBundle();
        bundle.setName("test-bundle");
        bundle.setServiceOwner(SERVICE_NAME);
        bundle.setDescription("A test bundle");

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bundle);

        mockMvc.perform(post(API_BUNDLES)
                .with(user(ADMIN_USER).roles(ADMIN_ROLE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("test-bundle")));
    }

    @Test
    public void testCreateBundleValidationFailNoService() throws Exception {
        PolicyBundle bundle = new PolicyBundle();
        bundle.setName("orphan-bundle");
        // No service owner

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bundle);

        mockMvc.perform(post(API_BUNDLES)
                .with(user(ADMIN_USER).roles(ADMIN_ROLE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("FUNC_BUNDLE_NO_SERVICE")));
    }

    @Test
    public void testCreateBundleValidationFailServiceNotFound() throws Exception {
        PolicyBundle bundle = new PolicyBundle();
        bundle.setName("unknown-service-bundle");
        bundle.setServiceOwner("does-not-exist");

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bundle);

        mockMvc.perform(post(API_BUNDLES)
                .with(user(ADMIN_USER).roles(ADMIN_ROLE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("FUNC_Vk_SERVICE_NOT_FOUND")));
    }

    @Test
    public void testGetAllBundlesSuccess() throws Exception {
        // Create a couple of bundles
        PolicyBundle b1 = new PolicyBundle();
        b1.setName("bundle-1");
        b1.setServiceOwner(SERVICE_NAME);
        bundleRepository.save(b1);

        PolicyBundle b2 = new PolicyBundle();
        b2.setName("bundle-2");
        b2.setServiceOwner(SERVICE_NAME);
        bundleRepository.save(b2);

        mockMvc.perform(get(API_BUNDLES)
                .with(user(ADMIN_USER).roles(ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    public void testSearchBundlesByName() throws Exception {
        PolicyBundle b1 = new PolicyBundle();
        b1.setName("alpha-bundle");
        b1.setServiceOwner(SERVICE_NAME);
        bundleRepository.save(b1);

        mockMvc.perform(get(API_BUNDLES)
                .with(user(ADMIN_USER).roles(ADMIN_ROLE))
                .param("search", "alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("alpha-bundle")));
    }

    @Test
    public void testBuildBundleSuccess() throws Exception {
        PolicyBundle b1 = new PolicyBundle();
        b1.setName("buildable-bundle");
        b1.setServiceOwner(SERVICE_NAME);
        bundleRepository.save(b1);

        mockMvc.perform(post(API_BUNDLES + "/buildable-bundle/build")
                .with(user(ADMIN_USER).roles(ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Build triggered")));
    }

    @Test
    public void testDownloadBundleByNameSuccess() throws Exception {
        PolicyBundle b1 = new PolicyBundle();
        b1.setName("download-me");
        b1.setServiceOwner(SERVICE_NAME);
        bundleRepository.save(b1);

        // We aren't testing the full TAR content structure here (covered by other
        // tests),
        // just the controller mapping and basic response
        mockMvc.perform(get(API_BUNDLES + "/download-me/download")
                .with(user(ADMIN_USER).roles(ADMIN_ROLE))
                .param("service", SERVICE_NAME))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gzip"));
    }
}
