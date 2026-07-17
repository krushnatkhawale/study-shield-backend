package com.studyshield.quiz.controller;

import com.studyshield.quiz.dto.QuizAttemptRequest;
import com.studyshield.quiz.dto.QuizAttemptResponse;
import com.studyshield.quiz.entity.QuizAttempt;
import com.studyshield.quiz.repository.QuizAttemptRepository;
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
class QuizAttemptControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() { quizAttemptRepository.deleteAll(); }

    @Test
    void shouldCreateQuizAttempt() throws Exception {
        QuizAttemptRequest request = new QuizAttemptRequest(1L, 1L, 1L, 10);
        mockMvc.perform(post("/api/v1/quiz-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quizId").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldGetQuizAttemptById() throws Exception {
        QuizAttempt qa = quizAttemptRepository.save(
                QuizAttempt.builder().quizId(1L).childProfileId(1L).userId(1L).totalQuestions(10).build());
        mockMvc.perform(get("/api/v1/quiz-attempts/{id}", qa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(1));
    }

    @Test
    void shouldGetByUserId() throws Exception {
        quizAttemptRepository.save(QuizAttempt.builder().quizId(1L).childProfileId(1L).userId(1L).totalQuestions(10).build());
        mockMvc.perform(get("/api/v1/quiz-attempts/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldCompleteQuizAttempt() throws Exception {
        QuizAttempt qa = quizAttemptRepository.save(
                QuizAttempt.builder().quizId(1L).childProfileId(1L).userId(1L).totalQuestions(10).build());
        mockMvc.perform(put("/api/v1/quiz-attempts/{id}/complete", qa.getId())
                        .param("correctAnswers", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.correctAnswers").value(8))
                .andExpect(jsonPath("$.score").value(80));
    }

    @Test
    void shouldDeleteQuizAttempt() throws Exception {
        QuizAttempt qa = quizAttemptRepository.save(
                QuizAttempt.builder().quizId(1L).childProfileId(1L).userId(1L).totalQuestions(10).build());
        mockMvc.perform(delete("/api/v1/quiz-attempts/{id}", qa.getId()))
                .andExpect(status().isNoContent());
    }
}
