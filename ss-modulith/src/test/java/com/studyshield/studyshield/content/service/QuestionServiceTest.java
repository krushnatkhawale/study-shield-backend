package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuestionOptionDto;
import com.studyshield.studyshield.content.dto.QuestionRequest;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.entity.Question;
import com.studyshield.studyshield.content.entity.QuestionType;
import com.studyshield.studyshield.content.entity.Quiz;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.content.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Question versioning — every edit must keep the old content reachable as a revision, never
 * overwrite history, and a quiz must always be served the latest version of each question.
 */
class QuestionServiceTest {

    private QuestionRepository questionRepository;
    private QuizRepository quizRepository;
    private QuestionService service;

    private static final Quiz QUIZ = quiz();
    private long nextId = 100L;

    @BeforeEach
    void setUp() {
        questionRepository = mock(QuestionRepository.class);
        quizRepository = mock(QuizRepository.class);
        service = new QuestionService(questionRepository, quizRepository);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(QUIZ));
        // Hibernate assigns the id on save; mirror that so mapToResponse sees a real id.
        when(questionRepository.save(any())).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            if (q.getId() == null) {
                q.setId(nextId++);
            }
            return q;
        });
    }

    @Test
    void createStartsAVersionGroupAtVersionOne() {
        QuestionResponse created = service.create(request("What is 2 + 2?", "a"));

        assertThat(created.version()).isEqualTo(1);
        assertThat(created.versionGroupId()).isNotBlank();
        assertThat(created.questionText()).isEqualTo("What is 2 + 2?");
    }

    @Test
    void editingAQuestionCreatesANewVersionAndSupersedesTheOldOne() {
        Question v1 = question(10L, "What is 2 + 2?", List.of("a"));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(v1));

        QuestionResponse v2 = service.update(10L, request("What is 2 + 2 × 3?", "b"));

        assertThat(v2.id()).isNotEqualTo(10L);
        assertThat(v2.version()).isEqualTo(2);
        assertThat(v2.versionGroupId()).isEqualTo(v1.getVersionGroupId());
        // Old version is now superseded by the new row
        ArgumentCaptor<Question> saved = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository, times(2)).save(saved.capture());
        Question savedV1 = saved.getAllValues().get(1);
        assertThat(savedV1.getId()).isEqualTo(10L);
        assertThat(savedV1.getSupersededBy()).isNotNull();
        assertThat(savedV1.getSupersededBy().getVersionNumber()).isEqualTo(2);
    }

    @Test
    void savingIdenticalContentDoesNotCreateAnEmptyRevision() {
        Question v1 = question(10L, "What is 2 + 2?", List.of("a"));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(v1));

        QuestionResponse result = service.update(10L, request("What is 2 + 2?", "a"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.version()).isEqualTo(1);
        verify(questionRepository, atMostOnce()).save(any());
    }

    @Test
    void quizFetchesOnlyTheLatestVersionOfEachQuestion() {
        Question v1 = question(10L, "old text", List.of("a"));
        Question v2 = question(11L, "new text", List.of("b"));
        v2.setVersionGroupId(v1.getVersionGroupId());
        v2.setVersionNumber(2);
        when(questionRepository.findByQuizIdAndSupersededByNull(1L)).thenReturn(List.of(v2));

        List<QuestionResponse> byQuiz = service.getByQuizId(1L);

        assertThat(byQuiz).hasSize(1);
        assertThat(byQuiz.getFirst().questionText()).isEqualTo("new text");
    }

    @Test
    void revisionsAreReturnedOldestFirstForTheWholeGroup() {
        Question v1 = question(10L, "v1 text", List.of("a"));
        v1.setVersionNumber(1);
        Question v2 = question(11L, "v2 text", List.of("b"));
        v2.setVersionGroupId(v1.getVersionGroupId());
        v2.setVersionNumber(2);
        when(questionRepository.findById(10L)).thenReturn(Optional.of(v1));
        when(questionRepository.findByVersionGroupIdOrderByVersionNumberAsc(v1.getVersionGroupId()))
                .thenReturn(List.of(v1, v2));

        List<QuestionResponse> revisions = service.getRevisions(10L);

        assertThat(revisions).hasSize(2);
        assertThat(revisions).extracting(QuestionResponse::version).containsExactly(1, 2);
        assertThat(revisions).extracting(QuestionResponse::questionText)
                .containsExactly("v1 text", "v2 text");
    }

    @Test
    void deletingAQuestionRemovesTheWholeVersionGroup() {
        Question v1 = question(10L, "v1", List.of("a"));
        Question v2 = question(11L, "v2", List.of("b"));
        v2.setVersionGroupId("group-1");
        v2.setVersionNumber(2);
        when(questionRepository.findById(10L)).thenReturn(Optional.of(v1));
        when(questionRepository.findByVersionGroupId(v1.getVersionGroupId())).thenReturn(List.of(v1, v2));

        service.delete(10L);

        verify(questionRepository).delete(v1);
        verify(questionRepository).delete(v2);
    }

    private static QuestionRequest request(String text, String correctId) {
        return new QuestionRequest(
                null, text, null, QuestionType.SINGLE_CHOICE,
                List.of(
                        new QuestionOptionDto("a", "A option", null),
                        new QuestionOptionDto("b", "B option", null),
                        new QuestionOptionDto("c", "C option", null),
                        new QuestionOptionDto("d", "D option", null)),
                List.of(correctId),
                null, 1, com.studyshield.studyshield.content.entity.Difficulty.EASY,
                List.of("English"), List.of(), 1L, false, 0, null);
    }

    private static Question question(Long id, String text, List<String> correct) {
        Question q = Question.builder()
                .questionText(text)
                .correctOption(correct.getFirst())
                .optionA("A option")
                .optionB("B option")
                .optionC("C option")
                .optionD("D option")
                .options(List.of(
                        new com.studyshield.studyshield.content.entity.QuestionOption("a", "A option", null),
                        new com.studyshield.studyshield.content.entity.QuestionOption("b", "B option", null),
                        new com.studyshield.studyshield.content.entity.QuestionOption("c", "C option", null),
                        new com.studyshield.studyshield.content.entity.QuestionOption("d", "D option", null)))
                .correctAnswers(correct)
                .quiz(QUIZ)
                .versionGroupId("group-" + id)
                .versionNumber(1)
                .build();
        q.setId(id);
        return q;
    }

    private static Quiz quiz() {
        Quiz q = new Quiz();
        q.setId(1L);
        return q;
    }
}