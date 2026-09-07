package com.studyshield.studyshield.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.user.dto.UserRequest;
import com.studyshield.studyshield.user.dto.auth.AuthResponse;
import com.studyshield.studyshield.user.dto.auth.SignInRequest;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserTypeEnforcementTest {

    private static final String MOBILE_EMAIL = "parent.one@test.com";
    private static final String MOBILE_PASSWORD = "mobilePass123";
    private static final String ADMIN_EMAIL = "admin.one@test.com";
    private static final String ADMIN_PASSWORD = "adminPass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        if (!userService.existsByEmail(MOBILE_EMAIL)) {
            userService.create(new UserRequest(MOBILE_EMAIL, MOBILE_PASSWORD, "Parent One",
                    null, User.UserRole.PARENT.name(), null, true));
        }
        if (!userService.existsByEmail(ADMIN_EMAIL)) {
            userService.createAdminUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin One", null, true);
        }
    }

    @Test
    void mobileSigninAcceptsMobileUserAndRejectsAdminUser() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignInRequest(MOBILE_EMAIL, MOBILE_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Success"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignInRequest(ADMIN_EMAIL, ADMIN_PASSWORD))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_MOBILE"));
    }

    @Test
    void adminSigninAcceptsOnlyAdminUser() throws Exception {
        mockMvc.perform(post("/api/auth/admin-signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignInRequest(MOBILE_EMAIL, MOBILE_PASSWORD))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_ADMIN"));

        mockMvc.perform(post("/api/auth/admin-signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignInRequest(ADMIN_EMAIL, ADMIN_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty());
    }

    @Test
    void adminUsersApiIsGatedToAdminRole() throws Exception {
        String mobileToken = mobileSignIn(MOBILE_EMAIL, MOBILE_PASSWORD);

        mockMvc.perform(get("/api/v1/admin-users")
                        .header("Authorization", "Bearer " + mobileToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin-users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUsersApiCreatesListsAndGuardsSelfDeletion() throws Exception {
        String adminToken = adminSignIn(ADMIN_EMAIL, ADMIN_PASSWORD);

        MvcResult created = mockMvc.perform(post("/api/v1/admin-users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin.two@test.com","password":"secondAdmin123",
                                 "name":"Admin Two","active":true}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        long secondAdminId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/admin-users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(delete("/api/v1/admin-users/{id}", secondAdminId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin-users/{id}", selfId(adminToken))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mobileUserManagementApiIsGatedToAdminRole() throws Exception {
        String mobileToken = mobileSignIn(MOBILE_EMAIL, MOBILE_PASSWORD);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + mobileToken))
                .andExpect(status().isForbidden());
    }

    private String adminSignIn(String loginId, String password) throws Exception {
        return tokenFrom("/api/auth/admin-signin", loginId, password);
    }

    private String mobileSignIn(String loginId, String password) throws Exception {
        return tokenFrom("/api/auth/signin", loginId, password);
    }

    private String tokenFrom(String path, String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SignInRequest(loginId, password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("sessionId").asText();
    }

    private long selfId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin-users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode user : node) {
            if (user.get("email").asText().equals(ADMIN_EMAIL)) {
                return user.get("id").asLong();
            }
        }
        throw new AssertionError("self account missing from admin-users list");
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}