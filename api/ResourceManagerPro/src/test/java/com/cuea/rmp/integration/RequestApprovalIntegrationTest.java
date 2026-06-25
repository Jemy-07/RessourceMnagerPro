package com.cuea.rmp.integration;

import com.cuea.rmp.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestApprovalIntegrationTest extends AbstractIntegrationTest {

    @Test
    void approving_a_request_creates_the_assignment() throws Exception {
        String token = adminToken();

        String resourceId = createAndGetId(token, "/api/v1/resources", """
                {"orgId":"%s","name":"Req Res","type":"HUMAN","hourlyRateAmount":70,"currency":"USD"}"""
                .formatted(ORG_ID));

        String projectId = createAndGetId(token, "/api/v1/projects", """
                {"orgId":"%s","managerId":"%s","name":"Req Proj","description":"d",
                 "startDate":"2027-07-01","endDate":"2027-12-31"}"""
                .formatted(ORG_ID, ORG_ID));

        String requestId = createAndGetId(token, "/api/v1/requests", """
                {"resourceId":"%s","projectId":"%s","title":"Need Req Res",
                 "startDate":"2027-09-01","endDate":"2027-09-05","allocationPct":50}"""
                .formatted(resourceId, projectId));

        // Approve → request APPROVED, assignment created.
        mockMvc.perform(post("/api/v1/requests/{id}/approve", requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // The project now has an assignment for that resource.
        mockMvc.perform(get("/api/v1/projects/{id}/assignments", projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].resourceId").value(resourceId))
                .andExpect(jsonPath("$.data[0].allocationPct").value(50));

        // A second decision on the same request → 409.
        mockMvc.perform(post("/api/v1/requests/{id}/approve", requestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("REQUEST_ALREADY_DECIDED"));
    }
}
