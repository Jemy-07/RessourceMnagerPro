package com.cuea.rmp.integration;

import com.cuea.rmp.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SyncIntegrationTest extends AbstractIntegrationTest {

    @Test
    void push_with_stale_version_resolves_last_write_wins_then_pull_returns_it() throws Exception {
        String token = adminToken();

        String resourceId = createAndGetId(token, "/api/v1/resources", """
                {"orgId":"%s","name":"Sync Res","type":"HUMAN","hourlyRateAmount":40,"currency":"USD"}"""
                .formatted(ORG_ID));

        // Bump server version to 1 so a client at v0 is stale.
        mockMvc.perform(put("/api/v1/resources/{id}", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sync Res v1","hourlyRateAmount":45,"currency":"USD",
                                 "availabilityStatus":"AVAILABLE"}"""))
                .andExpect(status().isOk());

        // Push a stale-version edit with a far-future clientUpdatedAt → client wins (LWW) + conflict.
        String push = """
                {"changes":[
                  {"entityType":"RESOURCE","id":"%s",
                   "payload":{"name":"Sync Res (offline edit)"},
                   "clientUpdatedAt":"2030-01-01T00:00:00Z","clientVersion":0,"deleted":false}
                ]}""".formatted(resourceId);
        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(push))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appliedCount").value(1))
                .andExpect(jsonPath("$.data.conflictCount").value(1))
                .andExpect(jsonPath("$.data.conflicts[0].resolution").value("CLIENT_WON"));

        // Pull from epoch returns the row with the applied edit.
        mockMvc.perform(get("/api/v1/sync/pull")
                        .param("since", "1970-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serverTime").isNotEmpty())
                .andExpect(jsonPath("$.data.changes[?(@.id=='" + resourceId + "')]").isNotEmpty());
    }
}
