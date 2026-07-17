package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.FreemiumPackRequest;
import com.studyshield.content.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FreemiumPackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FreemiumPackRepository freemiumPackRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ContentPackRepository contentPackRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ClassGradeRepository classGradeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @BeforeEach
    void setUp() {
        freemiumPackRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        contentPackRepository.deleteAll();
        subjectRepository.deleteAll();
        classGradeRepository.deleteAll();
        boardRepository.deleteAll();
    }

    @Test
    void shouldIssueFreemiumPackWithQuizzesAndQuestions() throws Exception {
        FreemiumPackRequest request = new FreemiumPackRequest(
                "1", "all", "English", null, "device-abc", null, false);

        mockMvc.perform(post("/api/v1/freemium/packs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packId").isNumber())
                .andExpect(jsonPath("$.className").value("Class 1"))
                .andExpect(jsonPath("$.freemiumQuizzesPerSubject").value(5))
                .andExpect(jsonPath("$.quizCount").value(20)) // 4 subjects × 5
                .andExpect(jsonPath("$.subjects", hasSize(4)))
                .andExpect(jsonPath("$.quizzes", hasSize(20)))
                .andExpect(jsonPath("$.quizzes[0].questions", hasSize(10)))
                .andExpect(jsonPath("$.quizzes[0].questions[0].options").isArray())
                .andExpect(jsonPath("$.quizzes[0].questions[0].correctAnswers").isArray())
                .andExpect(jsonPath("$.quizzes[0].contentTier").value("FREEMIUM"));
    }

    @Test
    void shouldBeIdempotentForSameDeviceAndClass() throws Exception {
        FreemiumPackRequest request = new FreemiumPackRequest(
                "1", "all", "English", null, "device-xyz", null, false);

        MvcResult first = mockMvc.perform(post("/api/v1/freemium/packs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Long packId = objectMapper.readTree(first.getResponse().getContentAsString()).get("packId").asLong();

        mockMvc.perform(post("/api/v1/freemium/packs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packId").value(packId));
    }

    @Test
    void shouldGetPackById() throws Exception {
        FreemiumPackRequest request = new FreemiumPackRequest(
                "Nursery", "all", "English", null, "device-get", null, false);

        MvcResult created = mockMvc.perform(post("/api/v1/freemium/packs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        Long packId = objectMapper.readTree(created.getResponse().getContentAsString()).get("packId").asLong();

        mockMvc.perform(get("/api/v1/freemium/packs/{id}", packId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packId").value(packId))
                .andExpect(jsonPath("$.quizzes").isArray());
    }

    @Test
    void shouldRejectMissingHolder() throws Exception {
        FreemiumPackRequest request = new FreemiumPackRequest(
                "1", "all", "English", null, null, null, false);

        mockMvc.perform(post("/api/v1/freemium/packs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
