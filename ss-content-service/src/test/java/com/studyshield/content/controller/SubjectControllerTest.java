package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.SubjectRequest;
import com.studyshield.content.entity.Board;
import com.studyshield.content.entity.ClassGrade;
import com.studyshield.content.entity.Subject;
import com.studyshield.content.repository.BoardRepository;
import com.studyshield.content.repository.ClassGradeRepository;
import com.studyshield.content.repository.SubjectRepository;
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
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ClassGradeRepository classGradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ClassGrade classGrade;

    @BeforeEach
    void setUp() {
        subjectRepository.deleteAll();
        classGradeRepository.deleteAll();
        boardRepository.deleteAll();
        Board board = boardRepository.save(new Board("CBSE", "CBSE", null, true));
        classGrade = classGradeRepository.save(
                ClassGrade.builder().gradeNumber(10).name("Class 10").board(board).build());
    }

    @Test
    void shouldCreateSubject() throws Exception {
        SubjectRequest request = new SubjectRequest("Mathematics", "MATH", "Mathematics subject", classGrade.getId(), true);

        mockMvc.perform(post("/api/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mathematics"))
                .andExpect(jsonPath("$.classGradeId").value(classGrade.getId()));
    }

    @Test
    void shouldGetSubjectById() throws Exception {
        Subject subject = subjectRepository.save(
                Subject.builder().name("Mathematics").code("MATH").classGrade(classGrade).active(true).build());

        mockMvc.perform(get("/api/v1/subjects/{id}", subject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mathematics"));
    }

    @Test
    void shouldGetAllSubjects() throws Exception {
        subjectRepository.save(Subject.builder().name("Math").code("MATH").classGrade(classGrade).active(true).build());
        subjectRepository.save(Subject.builder().name("Science").code("SCI").classGrade(classGrade).active(true).build());

        mockMvc.perform(get("/api/v1/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetSubjectsByClassGradeId() throws Exception {
        subjectRepository.save(Subject.builder().name("Math").code("MATH").classGrade(classGrade).active(true).build());

        mockMvc.perform(get("/api/v1/subjects/class-grade/{classGradeId}", classGrade.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldUpdateSubject() throws Exception {
        Subject subject = subjectRepository.save(
                Subject.builder().name("Mathematics").code("MATH").classGrade(classGrade).active(true).build());

        SubjectRequest request = new SubjectRequest("Mathematics Updated", "MATH", null, classGrade.getId(), true);

        mockMvc.perform(put("/api/v1/subjects/{id}", subject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mathematics Updated"));
    }

    @Test
    void shouldDeleteSubject() throws Exception {
        Subject subject = subjectRepository.save(
                Subject.builder().name("Mathematics").code("MATH").classGrade(classGrade).active(true).build());

        mockMvc.perform(delete("/api/v1/subjects/{id}", subject.getId()))
                .andExpect(status().isNoContent());
    }
}
