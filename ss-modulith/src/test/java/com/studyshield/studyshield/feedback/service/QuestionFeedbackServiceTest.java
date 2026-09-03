package com.studyshield.studyshield.feedback.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackRequest;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackResponse;
import com.studyshield.studyshield.feedback.entity.DownCategory;
import com.studyshield.studyshield.feedback.entity.FeedbackVote;
import com.studyshield.studyshield.feedback.entity.QuestionFeedback;
import com.studyshield.studyshield.feedback.repository.QuestionFeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Business-readable checks for question review feedback (up / down / report + comment).
 * Each test describes a rule a parent or content reviewer relies on.
 */
class QuestionFeedbackServiceTest {

    private static final long QUESTION_ID = 42L;
    private static final long ACCOUNT_ID = 7L;

    private QuestionFeedbackRepository feedbackRepository;
    private QuestionRepository questionRepository;
    private QuestionFeedbackService service;

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(QuestionFeedbackRepository.class);
        questionRepository = mock(QuestionRepository.class);
        service = new QuestionFeedbackService(feedbackRepository, questionRepository);
        when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
        when(feedbackRepository.save(any(QuestionFeedback.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void anUpVotePersistsOncePerUserWithOptionalComment() {
        QuestionFeedbackResponse response = service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("UP", null, "Great question", false));

        assertThat(response.vote()).isEqualTo(FeedbackVote.UP);
        assertThat(response.comment()).isEqualTo("Great question");
        assertThat(response.reported()).isFalse();
    }

    @Test
    void aDownVotePersistsWithCategoryAndOptionalComment() {
        QuestionFeedbackResponse response = service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("DOWN", "WRONG_ANSWER", null, false));

        assertThat(response.vote()).isEqualTo(FeedbackVote.DOWN);
        assertThat(response.downCategory()).isEqualTo(DownCategory.WRONG_ANSWER);
    }

    @Test
    void reportingAQuestionRequiresAMandatoryComment() {
        assertThatThrownBy(() -> service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("NONE", null, "  ", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comment is required");
    }

    @Test
    void reportingSetsTheReportedFlagAndKeepsTheVote() {
        QuestionFeedbackResponse response = service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("UP", null, "Inappropriate image", true));

        assertThat(response.reported()).isTrue();
        assertThat(response.vote()).isEqualTo(FeedbackVote.UP);
    }

    @Test
    void tappingUpAgainClearsTheOncePerUserVote() {
        service.submit(QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("UP", null, null, false));
        QuestionFeedbackResponse cleared = service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("NONE", null, null, false));

        assertThat(cleared.vote()).isEqualTo(FeedbackVote.NONE);
    }

    @Test
    void switchingFromUpToDownUpdatesTheSameRowNotADuplicate() {
        QuestionFeedback existing = new QuestionFeedback();
        existing.setId(1L);
        existing.setAccountId(ACCOUNT_ID);
        existing.setQuestionId(QUESTION_ID);
        existing.setVote(FeedbackVote.UP);
        when(feedbackRepository.findByAccountIdAndQuestionId(ACCOUNT_ID, QUESTION_ID)).thenReturn(Optional.of(existing));

        QuestionFeedbackResponse response = service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("DOWN", "TYPO", "Spelling", false));

        assertThat(response.vote()).isEqualTo(FeedbackVote.DOWN);
        assertThat(response.downCategory()).isEqualTo(DownCategory.TYPO);
    }

    @Test
    void feedbackOnANonExistentQuestionIsRejected() {
        when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);
        assertThatThrownBy(() -> service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("UP", null, null, false)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void anInvalidVoteValueIsRejected() {
        assertThatThrownBy(() -> service.submit(
                QUESTION_ID, ACCOUNT_ID, new QuestionFeedbackRequest("SIDEWAYS", null, null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vote");
    }

    @Test
    void aQuestionTheUserHasNotRatedReadsBackAsNoVote() {
        when(feedbackRepository.findByAccountIdAndQuestionId(ACCOUNT_ID, QUESTION_ID)).thenReturn(Optional.empty());
        QuestionFeedbackResponse response = service.getForUser(QUESTION_ID, ACCOUNT_ID);

        assertThat(response.vote()).isEqualTo(FeedbackVote.NONE);
        assertThat(response.reported()).isFalse();
    }
}
