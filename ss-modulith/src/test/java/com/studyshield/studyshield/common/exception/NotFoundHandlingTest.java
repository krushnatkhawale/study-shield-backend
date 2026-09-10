package com.studyshield.studyshield.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotFoundHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownRouteUnderPermittedPathReturns404() throws Exception {
        mockMvc.perform(post("/api/auth/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unknownRouteOutsidePermittedPathIsRejectedByAuth() throws Exception {
        mockMvc.perform(get("/api/definitely-not-a-route"))
                .andExpect(status().isUnauthorized());
    }
}