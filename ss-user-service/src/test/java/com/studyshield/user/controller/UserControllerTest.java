package com.studyshield.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.user.dto.UserRequest;
import com.studyshield.user.entity.User;
import com.studyshield.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() { userRepository.deleteAll(); }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest request = new UserRequest("test@test.com", "Test User", "1234567890", "PARENT", true);
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = userRepository.save(User.builder().email("test@test.com").name("Test").role(User.UserRole.PARENT).active(true).build());
        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        userRepository.save(User.builder().email("a@test.com").name("A").role(User.UserRole.PARENT).active(true).build());
        userRepository.save(User.builder().email("b@test.com").name("B").role(User.UserRole.PARENT).active(true).build());
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = userRepository.save(User.builder().email("test@test.com").name("Test").role(User.UserRole.PARENT).active(true).build());
        UserRequest request = new UserRequest("test@test.com", "Updated Name", "9999999999", "PARENT", true);
        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = userRepository.save(User.builder().email("test@test.com").name("Test").role(User.UserRole.PARENT).active(true).build());
        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }
}
