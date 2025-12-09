package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.model.Entitlement;
import org.openpickles.policy.engine.repository.EntitlementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class EntitlementSyncIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EntitlementRepository entitlementRepository;

        @BeforeEach
        public void cleanup() {
                entitlementRepository.deleteAll();
        }

        @Test
        @WithMockUser // Simulate an authenticated user
        public void testSyncEntitlements_Upsert() throws Exception {
                // 1. Initial Sync: Create new entitlement
                Entitlement e1 = new Entitlement();
                e1.setSubjectType(Entitlement.SubjectType.USER);
                e1.setSubjectId("alice");
                e1.setResourceType("DOCUMENT");
                e1.setResourceIds(Set.of("doc1", "doc2"));
                e1.setActions(Set.of("VIEW"));
                e1.setEffect(Entitlement.Effect.ALLOW);

                mockMvc.perform(post("/api/v1/entitlements/sync")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(List.of(e1))))
                                .andExpect(status().isOk());

                // Verify creation
                List<Entitlement> saved = entitlementRepository.findByResourceTypeAndSubjectTypeAndSubjectId("DOCUMENT",
                                Entitlement.SubjectType.USER, "alice");
                assertEquals(1, saved.size());
                assertEquals(2, saved.get(0).getResourceIds().size());
                assertEquals("VIEW", saved.get(0).getActions().iterator().next());

                // 2. Second Sync: Update existing entitlement
                Entitlement e1Update = new Entitlement();
                e1Update.setSubjectType(Entitlement.SubjectType.USER);
                e1Update.setSubjectId("alice");
                e1Update.setResourceType("DOCUMENT");
                e1Update.setResourceIds(Set.of("doc1", "doc2", "doc3")); // Added doc3
                e1Update.setActions(Set.of("EDIT")); // Changed action
                e1Update.setEffect(Entitlement.Effect.ALLOW);

                mockMvc.perform(post("/api/v1/entitlements/sync")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(List.of(e1Update))))
                                .andExpect(status().isOk());

                // Verify update (should still be 1 record, but updated)
                saved = entitlementRepository.findByResourceTypeAndSubjectTypeAndSubjectId("DOCUMENT",
                                Entitlement.SubjectType.USER, "alice");
                assertEquals(1, saved.size());
                assertEquals(3, saved.get(0).getResourceIds().size()); // Should have 3 now
                assertEquals("EDIT", saved.get(0).getActions().iterator().next());
        }
}
