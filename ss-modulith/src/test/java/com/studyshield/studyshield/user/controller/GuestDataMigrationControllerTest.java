package com.studyshield.studyshield.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.quizresult.dto.QuizResultRequest;
import com.studyshield.studyshield.user.dto.auth.ClaimGuestDataRequest;
import com.studyshield.studyshield.user.dto.auth.GuestAuthRequest;
import com.studyshield.studyshield.user.dto.auth.SignUpRequest;
import com.studyshield.studyshield.user.dto.auth.StudentRequest;
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
class GuestDataMigrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void guestDataMovesToNewAccountOnClaim() throws Exception {
        String deviceId = "device-migrate-001";
        String guestToken = tokenOf(guestAuth(deviceId));

        // Guest saves a quiz result under the guest account.
        saveQuizResult(guestToken, "Kid 1", 5);

        // Someone signs up on the same device and claims the guest data.
        String signUpToken = tokenOf(signUp("newuser-migrate-001@test.test"));

        mockMvc.perform(post("/api/migrate/guest-data")
                        .header("Authorization", "Bearer " + signUpToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClaimGuestDataRequest(deviceId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resultsMoved").value(1));

        // The result is no longer visible to the guest account...
        mockMvc.perform(get("/api/quiz-results")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // ...but it is visible to the new account.
        mockMvc.perform(get("/api/quiz-results")
                        .header("Authorization", "Bearer " + signUpToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].childName").value("Kid 1"))
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    void claimIsNoOp_whenGuestHasNoData() throws Exception {
        String token = tokenOf(signUp("newuser-migrate-002@test.test"));

        mockMvc.perform(post("/api/migrate/guest-data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClaimGuestDataRequest("device-never-used"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resultsMoved").value(0));
    }

    @Test
    void claimRejectsMissingDeviceId() throws Exception {
        String token = tokenOf(signUp("newuser-migrate-003@test.test"));

        mockMvc.perform(post("/api/migrate/guest-data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void claimRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/migrate/guest-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClaimGuestDataRequest("device-unauth"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void claimDoesNotMoveDataToAnotherRegisteredAccount() throws Exception {
        String deviceId = "device-migrate-004";
        String guestToken = tokenOf(guestAuth(deviceId));
        saveQuizResult(guestToken, "Kid 1", 3);

        String otherToken = tokenOf(signUp("other-user-migrate-004@test.test"));

        mockMvc.perform(post("/api/migrate/guest-data")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClaimGuestDataRequest(deviceId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultsMoved").value(1));

        // The guest result moved to the claiming account...
        mockMvc.perform(get("/api/quiz-results")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void claimMovesGuestChildrenAndRemovesSignupDefaultKid() throws Exception {
        String deviceId = "device-migrate-005";
        String guestToken = tokenOf(guestAuth(deviceId));

        // Guest adds a real child profile on the guest account.
        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new StudentRequest("Aarav", "M", 2019, "Nursery"))))
                .andExpect(status().isCreated());

        // Signing up auto-creates a default "Kid 1" child.
        String signUpToken = tokenOf(signUp("newuser-migrate-005@test.test"));
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + signUpToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Kid 1"));

        // Claiming the guest data moves the guest child and drops the default.
        mockMvc.perform(post("/api/migrate/guest-data")
                        .header("Authorization", "Bearer " + signUpToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClaimGuestDataRequest(deviceId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childrenMoved").value(1));

        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + signUpToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Aarav"));
    }

    private MvcResult guestAuth(String deviceId) throws Exception {
        return mockMvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GuestAuthRequest(deviceId))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult signUp(String loginId) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignUpRequest(loginId, "password123", "Parent"))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void saveQuizResult(String token, String childName, int score) throws Exception {
        mockMvc.perform(post("/api/quiz-results")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new QuizResultRequest(
                                childName, score, 5, 120L, "Quiz 1", null,
                                System.currentTimeMillis(), 0))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String tokenOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("sessionId").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}