package com.cuea.rmp.integration;

import com.cuea.rmp.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssignmentConflictIntegrationTest extends AbstractIntegrationTest {

    @Test
    void overlapping_allocation_over_100_percent_is_409() throws Exception {
        String token = adminToken();

        String resourceId = createAndGetId(token, "/api/v1/resources", """
                {"orgId":"%s","name":"Conflict Res","type":"HUMAN","hourlyRateAmount":50,"currency":"USD"}"""
                .formatted(ORG_ID));

        String projectId = createAndGetId(token, "/api/v1/projects", """
                {"orgId":"%s","managerId":"%s","name":"Conflict Proj","description":"d",
                 "startDate":"2027-07-01","endDate":"2027-12-31"}"""
                .formatted(ORG_ID, ORG_ID));

        // First assignment: 60% over Aug 1–15.
        mockMvc.perform(post("/api/v1/projects/{id}/assignments", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId":"%s","title":"Lead","startDate":"2027-08-01",
                                 "endDate":"2027-08-15","allocationPct":60}""".formatted(resourceId)))
                .andExpect(status().isCreated());

        // Overlapping 50% (60 + 50 > 100) → conflict.
        mockMvc.perform(post("/api/v1/projects/{id}/assignments", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId":"%s","title":"Reviewer","startDate":"2027-08-10",
                                 "endDate":"2027-08-20","allocationPct":50}""".formatted(resourceId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ASSIGNMENT_CONFLICT"));
    }
}
