package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.ClassGradeRequest;
import com.studyshield.content.entity.Board;
import com.studyshield.content.entity.ClassGrade;
import com.studyshield.content.repository.BoardRepository;
import com.studyshield.content.repository.ClassGradeRepository;
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
class ClassGradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ClassGradeRepository classGradeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Board board;

    @BeforeEach
    void setUp() {
        classGradeRepository.deleteAll();
        boardRepository.deleteAll();
        board = boardRepository.save(new Board("CBSE", "CBSE", null, true));
    }

    @Test
    void shouldCreateClassGrade() throws Exception {
        ClassGradeRequest request = new ClassGradeRequest(10, "Class 10", "Tenth Grade", board.getId());

        mockMvc.perform(post("/api/v1/class-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeNumber").value(10))
                .andExpect(jsonPath("$.boardId").value(board.getId()));
    }

    @Test
    void shouldGetClassGradeById() throws Exception {
        ClassGrade classGrade = classGradeRepository.save(
                ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());

        mockMvc.perform(get("/api/v1/class-grades/{id}", classGrade.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeNumber").value(10));
    }

    @Test
    void shouldGetAllClassGrades() throws Exception {
        classGradeRepository.save(ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());
        classGradeRepository.save(ClassGrade.builder().gradeNumber(12).name("Class 12").board(board).build());

        mockMvc.perform(get("/api/v1/class-grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetClassGradesByBoardId() throws Exception {
        classGradeRepository.save(ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());
        classGradeRepository.save(ClassGrade.builder().gradeNumber(12).name("Class 12").board(board).build());

        mockMvc.perform(get("/api/v1/class-grades/board/{boardId}", board.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldUpdateClassGrade() throws Exception {
        ClassGrade classGrade = classGradeRepository.save(
                ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());

        ClassGradeRequest request = new ClassGradeRequest(10, "Class 10 Updated", null, board.getId());

        mockMvc.perform(put("/api/v1/class-grades/{id}", classGrade.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Class 10 Updated"));
    }

    @Test
    void shouldDeleteClassGrade() throws Exception {
        ClassGrade classGrade = classGradeRepository.save(
                ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());

        mockMvc.perform(delete("/api/v1/class-grades/{id}", classGrade.getId()))
                .andExpect(status().isNoContent());
    }
}
