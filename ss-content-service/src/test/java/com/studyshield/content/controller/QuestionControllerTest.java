package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.QuestionOptionDto;
import com.studyshield.content.dto.QuestionRequest;
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
class QuestionControllerTest {

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

    private Quiz quiz;

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
        ContentPack contentPack = contentPackRepository.save(
                ContentPack.builder().name("Freemium Math").subject(subject).active(true).build());
        quiz = quizRepository.save(
                Quiz.builder().title("Math · Quiz 1").contentPack(contentPack)
                        .quizType(QuizType.STANDARD).questionCount(10)
                        .contentTier(ContentTier.FREEMIUM).freemiumIndex(1)
                        .language("English").active(true).build());
    }

    private List<QuestionOption> fourOptions() {
        return List.of(
                new QuestionOption("a", "3", null),
                new QuestionOption("b", "4", null),
                new QuestionOption("c", "5", null),
                new QuestionOption("d", "6", null)
        );
    }

    private List<QuestionOptionDto> fourOptionDtos() {
        return List.of(
                new QuestionOptionDto("a", "3", null),
                new QuestionOptionDto("b", "4", null),
                new QuestionOptionDto("c", "5", null),
                new QuestionOptionDto("d", "6", null)
        );
    }

    private Question sampleQuestion(String resourceId, String text, String correctId, int order, boolean blacklisted) {
        return Question.builder()
                .resourceId(resourceId)
                .questionText(text)
                .questionType(QuestionType.SINGLE_CHOICE)
                .options(fourOptions())
                .correctAnswers(List.of(correctId))
                .points(1)
                .difficulty(Difficulty.EASY)
                .languages(List.of("English"))
                .tags(List.of("addition"))
                .quiz(quiz)
                .orderIndex(order)
                .blacklisted(blacklisted)
                .build();
    }

    @Test
    void shouldCreateQuestion() throws Exception {
        QuestionRequest request = new QuestionRequest(
                "c01_math_01",
                "What is 2+2?",
                null,
                QuestionType.SINGLE_CHOICE,
                fourOptionDtos(),
                List.of("b"),
                "2+2=4",
                1,
                Difficulty.EASY,
                List.of("English"),
                List.of("addition"),
                quiz.getId(),
                false,
                0
        );

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionText").value("What is 2+2?"))
                .andExpect(jsonPath("$.resourceId").value("c01_math_01"))
                .andExpect(jsonPath("$.questionType").value("SINGLE_CHOICE"))
                .andExpect(jsonPath("$.options", hasSize(4)))
                .andExpect(jsonPath("$.options[1].id").value("b"))
                .andExpect(jsonPath("$.correctAnswers[0]").value("b"))
                .andExpect(jsonPath("$.quizId").value(quiz.getId()));
    }

    @Test
    void shouldCreateFitbQuestion() throws Exception {
        QuestionRequest request = new QuestionRequest(
                "c01_math_fitb",
                "Water is also called _____",
                null,
                QuestionType.FITB,
                List.of(),
                List.of("H2O", "water"),
                null,
                1,
                Difficulty.EASY,
                List.of("English"),
                null,
                quiz.getId(),
                false,
                1
        );

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionType").value("FITB"))
                .andExpect(jsonPath("$.correctAnswers", hasSize(2)));
    }

    @Test
    void shouldGetQuestionById() throws Exception {
        Question question = questionRepository.save(
                sampleQuestion("c01_q01", "What is 2+2?", "b", 0, false));

        mockMvc.perform(get("/api/v1/questions/{id}", question.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("What is 2+2?"))
                .andExpect(jsonPath("$.options[0].id").value("a"));
    }

    @Test
    void shouldGetAllQuestions() throws Exception {
        questionRepository.save(sampleQuestion("q1", "Q1", "a", 0, false));
        questionRepository.save(sampleQuestion("q2", "Q2", "b", 1, true));

        mockMvc.perform(get("/api/v1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetQuestionsByQuizId() throws Exception {
        questionRepository.save(sampleQuestion("q1", "Q1", "a", 0, false));

        mockMvc.perform(get("/api/v1/questions/quiz/{quizId}", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetActiveQuestionsByQuizId() throws Exception {
        questionRepository.save(sampleQuestion("q1", "Q1", "a", 0, false));
        questionRepository.save(sampleQuestion("q2", "Q2", "b", 1, true));

        mockMvc.perform(get("/api/v1/questions/quiz/{quizId}/active", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldUpdateQuestion() throws Exception {
        Question question = questionRepository.save(
                sampleQuestion("c01_q01", "What is 2+2?", "b", 0, false));

        QuestionRequest request = new QuestionRequest(
                "c01_q01",
                "What is 3+3?",
                null,
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new QuestionOptionDto("a", "5", null),
                        new QuestionOptionDto("b", "6", null),
                        new QuestionOptionDto("c", "7", null),
                        new QuestionOptionDto("d", "8", null)
                ),
                List.of("b"),
                null,
                1,
                Difficulty.MEDIUM,
                List.of("English"),
                List.of("addition"),
                quiz.getId(),
                false,
                0
        );

        mockMvc.perform(put("/api/v1/questions/{id}", question.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("What is 3+3?"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
    }

    @Test
    void shouldDeleteQuestion() throws Exception {
        Question question = questionRepository.save(
                sampleQuestion("c01_q01", "What is 2+2?", "b", 0, false));

        mockMvc.perform(delete("/api/v1/questions/{id}", question.getId()))
                .andExpect(status().isNoContent());
    }
}
