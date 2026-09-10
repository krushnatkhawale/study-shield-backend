package com.studyshield.studyshield.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.user.dto.auth.GuestAuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuestAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void guestAuthReturnsRealSessionToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GuestAuthRequest("device-test-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Success"))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.get("loginId").asText()).startsWith("guest-");
    }

    @Test
    void guestAuthIsIdempotentPerDevice() throws Exception {
        String first = accountId("device-dup-456");
        String second = accountId("device-dup-456");
        assertThat(second).isEqualTo(first);
    }

    @Test
    void guestTokenCanAccessProtectedEndpoint() throws Exception {
        String token = tokenOf("device-protected-789");

        mockMvc.perform(get("/api/quiz-results")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void guestAuthRejectsMissingDeviceId() throws Exception {
        mockMvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private String accountId(String deviceId) throws Exception {
        return objectMapper.readTree(authBodyOf(deviceId).getResponse().getContentAsString())
                .get("accountId").asText();
    }

    private String tokenOf(String deviceId) throws Exception {
        return objectMapper.readTree(authBodyOf(deviceId).getResponse().getContentAsString())
                .get("sessionId").asText();
    }

    private MvcResult authBodyOf(String deviceId) throws Exception {
        return mockMvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GuestAuthRequest(deviceId))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}