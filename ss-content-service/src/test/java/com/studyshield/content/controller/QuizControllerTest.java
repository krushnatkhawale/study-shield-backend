package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.QuizRequest;
import com.studyshield.content.entity.*;
import com.studyshield.content.entity.Quiz.QuizType;
import com.studyshield.content.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ClassGradeRepository classGradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ContentPackRepository contentPackRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ContentPack contentPack;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        contentPackRepository.deleteAll();
        subjectRepository.deleteAll();
        classGradeRepository.deleteAll();
        boardRepository.deleteAll();

        Board board = boardRepository.save(new Board("CBSE", "CBSE", null, true));
        ClassGrade classGrade = classGradeRepository.save(
                ClassGrade.builder().gradeNumber(1).name("Class 1").board(board).build());
        Subject subject = subjectRepository.save(
                Subject.builder().name("Math").code("MATH").classGrade(classGrade).active(true).build());
        contentPack = contentPackRepository.save(
                ContentPack.builder().name("Freemium Math Pack").subject(subject).active(true).build());
    }

    @Test
    void shouldCreateQuiz() throws Exception {
        QuizRequest request = new QuizRequest(
                "Math · Quiz 1",
                "Freemium slot 1",
                contentPack.getId(),
                QuizType.STANDARD,
                10,
                ContentTier.FREEMIUM,
                1,
                "English",
                true
        );

        mockMvc.perform(post("/api/v1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Math · Quiz 1"))
                .andExpect(jsonPath("$.contentPackId").value(contentPack.getId()))
                .andExpect(jsonPath("$.questionCount").value(10))
                .andExpect(jsonPath("$.contentTier").value("FREEMIUM"))
                .andExpect(jsonPath("$.freemiumIndex").value(1))
                .andExpect(jsonPath("$.language").value("English"));
    }

    @Test
    void shouldGetQuizByIdWithQuestions() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder().title("Math · Quiz 1").contentPack(contentPack)
                        .quizType(QuizType.STANDARD).questionCount(10)
                        .contentTier(ContentTier.FREEMIUM).freemiumIndex(1)
                        .language("English").active(true).build());
        questionRepository.save(Question.builder()
                .resourceId("c01_q01")
                .questionText("What is 2+2?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .options(List.of(
                        new QuestionOption("a", "3", null),
                        new QuestionOption("b", "4", null),
                        new QuestionOption("c", "5", null),
                        new QuestionOption("d", "6", null)
                ))
                .correctAnswers(List.of("b"))
                .difficulty(Difficulty.EASY)
                .languages(List.of("English"))
                .quiz(quiz)
                .orderIndex(0)
                .build());

        mockMvc.perform(get("/api/v1/quizzes/{id}", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Math · Quiz 1"))
                .andExpect(jsonPath("$.questions", hasSize(1)))
                .andExpect(jsonPath("$.questions[0].correctAnswers[0]").value("b"))
                .andExpect(jsonPath("$.questions[0].options", hasSize(4)));
    }

    @Test
    void shouldGetAllQuizzesWithoutEmbeddedQuestions() throws Exception {
        quizRepository.save(Quiz.builder().title("Quiz 1").contentPack(contentPack)
                .quizType(QuizType.STANDARD).questionCount(10).active(true).build());
        quizRepository.save(Quiz.builder().title("Quiz 2").contentPack(contentPack)
                .quizType(QuizType.SINGLE).questionCount(1).active(true).build());

        mockMvc.perform(get("/api/v1/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].questions").doesNotExist());
    }

    @Test
    void shouldGetQuizzesByContentPackId() throws Exception {
        quizRepository.save(Quiz.builder().title("Quiz 1").contentPack(contentPack)
                .quizType(QuizType.STANDARD).questionCount(10).active(true).build());

        mockMvc.perform(get("/api/v1/quizzes/content-pack/{contentPackId}", contentPack.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldDownloadPackWithQuestions() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder().title("Math · Quiz 1").contentPack(contentPack)
                        .quizType(QuizType.STANDARD).questionCount(10)
                        .contentTier(ContentTier.FREEMIUM).freemiumIndex(1).active(true).build());
        questionRepository.save(Question.builder()
                .resourceId("c01_q01")
                .questionText("Sky colour?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .options(List.of(
                        new QuestionOption("a", "Blue", null),
                        new QuestionOption("b", "Red", null)
                ))
                .correctAnswers(List.of("a"))
                .difficulty(Difficulty.EASY)
                .languages(List.of("English"))
                .quiz(quiz)
                .orderIndex(0)
                .build());

        mockMvc.perform(get("/api/v1/quizzes/content-pack/{id}/download", contentPack.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].questions", hasSize(1)))
                .andExpect(jsonPath("$[0].questions[0].resourceId").value("c01_q01"));
    }

    @Test
    void shouldUpdateQuiz() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder().title("Algebra Quiz").contentPack(contentPack)
                        .quizType(QuizType.STANDARD).questionCount(10).active(true).build());

        QuizRequest request = new QuizRequest(
                "Algebra Quiz Updated", null, contentPack.getId(),
                QuizType.STANDARD, 10, ContentTier.PREMIUM, null, "English", true
        );

        mockMvc.perform(put("/api/v1/quizzes/{id}", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Algebra Quiz Updated"))
                .andExpect(jsonPath("$.contentTier").value("PREMIUM"));
    }

    @Test
    void shouldDeleteQuiz() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder().title("Algebra Quiz").contentPack(contentPack)
                        .quizType(QuizType.STANDARD).questionCount(10).active(true).build());

        mockMvc.perform(delete("/api/v1/quizzes/{id}", quiz.getId()))
                .andExpect(status().isNoContent());
    }
}
