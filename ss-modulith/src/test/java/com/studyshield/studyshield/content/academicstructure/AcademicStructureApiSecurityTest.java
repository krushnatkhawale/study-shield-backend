package com.studyshield.studyshield.content.academicstructure;

import com.studyshield.studyshield.user.dto.UserRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The academic-structure data loop is ADMIN-only: anonymous callers get 401,
 * mobile-role callers get 403, admins get through.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademicStructureApiSecurityTest {

    private static final String MOBILE_EMAIL = "parent.api@test.com";
    private static final String MOBILE_PASSWORD = "mobilePass123";
    private static final String ADMIN_EMAIL = "admin.api@test.com";
    private static final String ADMIN_PASSWORD = "adminPass123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private AcademicStructurePurgeService purgeService;

    @BeforeEach
    void setUp() {
        // The load guard is initialize-only; start from empty tables (shared test DB).
        purgeService.purge("all", true);
        if (!userService.existsByEmail(MOBILE_EMAIL)) {
            userService.create(new UserRequest(MOBILE_EMAIL, MOBILE_PASSWORD, "Parent API",
                    null, User.UserRole.PARENT.name(), null, true));
        }
        if (!userService.existsByEmail(ADMIN_EMAIL)) {
            userService.createAdminUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin API", null, true);
        }
    }

    @Test
    void anonymousCallersAreUnauthorizedOnEveryEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/academic-structure/status"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/academic-structure/load/all"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/academic-structure/export/boards"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/academic-structure/all")
                        .param("confirm", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mobileRoleCallersAreForbidden() throws Exception {
        String mobileToken = signIn(MOBILE_EMAIL, MOBILE_PASSWORD, "/api/auth/signin");

        mockMvc.perform(get("/api/v1/academic-structure/status")
                        .header("Authorization", "Bearer " + mobileToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/academic-structure/load/all")
                        .header("Authorization", "Bearer " + mobileToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminsCanSeeStatusAndRunADryRunLoad() throws Exception {
        String adminToken = signIn(ADMIN_EMAIL, ADMIN_PASSWORD, "/api/auth/admin-signin");

        mockMvc.perform(get("/api/v1/academic-structure/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entities").isArray());

        mockMvc.perform(post("/api/v1/academic-structure/load/all")
                        .param("dryRun", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dryRun").value(true));
    }

    private String signIn(String email, String password, String path) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(result.getResponse().getContentAsString());
        return node.path("sessionId").asText();
    }
}
