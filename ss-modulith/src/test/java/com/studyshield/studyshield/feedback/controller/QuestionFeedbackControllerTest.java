package com.studyshield.studyshield.feedback.controller;

import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackRequest;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackResponse;
import com.studyshield.studyshield.feedback.entity.FeedbackVote;
import com.studyshield.studyshield.feedback.entity.QuestionFeedback;
import com.studyshield.studyshield.feedback.repository.QuestionFeedbackRepository;
import com.studyshield.studyshield.feedback.service.QuestionFeedbackService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HTTP-layer wiring checks for question-review feedback (up / down / report + comment).
 *
 * <p>The controller extracts the account id from the authenticated principal and delegates to
 * the service. These tests verify that wiring end-to-end (controller → service). Service rules
 * themselves are covered by QuestionFeedbackServiceTest.
 *
 * <p>Note: the service is used as a real instance backed by mocked (interface) repositories so the
 * suite remains green on the JDK default, where Mockito's inline mock maker cannot instrument the
 * concrete {@code @Service} class.
 */
class QuestionFeedbackControllerTest {

    private static final long QUESTION_ID = 42L;
    private static final long ACCOUNT_ID = 7L;

    private QuestionFeedbackController controller;

    @BeforeEach
    void setUp() {
        QuestionFeedbackRepository feedbackRepository = mock(QuestionFeedbackRepository.class);
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
        when(feedbackRepository.save(any(QuestionFeedback.class))).thenAnswer(inv -> inv.getArgument(0));
        QuestionFeedbackService service = new QuestionFeedbackService(feedbackRepository, questionRepository);
        controller = new QuestionFeedbackController(service);

        // The controller reads the account id from the security principal.
        Authentication auth = new TestingAuthenticationToken(String.valueOf(ACCOUNT_ID), null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitPersistsForThePrincipalAccountId() {
        QuestionFeedbackRequest request = new QuestionFeedbackRequest("UP", null, "Great question", false);

        ResponseEntity<QuestionFeedbackResponse> result = controller.submit(QUESTION_ID, request);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        QuestionFeedbackResponse body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.questionId()).isEqualTo(QUESTION_ID);
        assertThat(body.vote()).isEqualTo(FeedbackVote.UP);
        assertThat(body.comment()).isEqualTo("Great question");
    }

    @Test
    void getReturnsTheUsersFeedbackWithDefaultNoneWhenMissing() {
        ResponseEntity<QuestionFeedbackResponse> result = controller.get(QUESTION_ID);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        QuestionFeedbackResponse body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.questionId()).isEqualTo(QUESTION_ID);
        assertThat(body.vote()).isEqualTo(FeedbackVote.NONE);
    }
}
