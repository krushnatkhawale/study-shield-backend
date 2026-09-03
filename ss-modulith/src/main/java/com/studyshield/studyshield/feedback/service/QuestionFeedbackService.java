package com.studyshield.studyshield.feedback.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackRequest;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackResponse;
import com.studyshield.studyshield.feedback.entity.DownCategory;
import com.studyshield.studyshield.feedback.entity.FeedbackVote;
import com.studyshield.studyshield.feedback.entity.QuestionFeedback;
import com.studyshield.studyshield.feedback.repository.QuestionFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionFeedbackService {

    private final QuestionFeedbackRepository feedbackRepository;
    private final QuestionRepository questionRepository;

    public QuestionFeedbackService(QuestionFeedbackRepository feedbackRepository,
                                   QuestionRepository questionRepository) {
        this.feedbackRepository = feedbackRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public QuestionFeedbackResponse submit(Long questionId, Long accountId, QuestionFeedbackRequest request) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question", questionId);
        }

        FeedbackVote vote = parseVote(request.vote());
        DownCategory downCategory = parseDownCategory(request.vote(), request.downCategory());
        String comment = normalize(request.comment());
        boolean report = Boolean.TRUE.equals(request.report());

        if (report && comment == null) {
            throw new IllegalArgumentException("A comment is required when reporting a question");
        }

        QuestionFeedback feedback = feedbackRepository
                .findByAccountIdAndQuestionId(accountId, questionId)
                .orElseGet(QuestionFeedback::new);

        feedback.setAccountId(accountId);
        feedback.setQuestionId(questionId);
        feedback.setVote(vote);
        feedback.setDownCategory(downCategory);
        feedback.setComment(comment);
        feedback.setReported(report);

        QuestionFeedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuestionFeedbackResponse getForUser(Long questionId, Long accountId) {
        return feedbackRepository.findByAccountIdAndQuestionId(accountId, questionId)
                .map(this::toResponse)
                .orElseGet(() -> new QuestionFeedbackResponse(null, questionId, FeedbackVote.NONE,
                        null, false, null, null, null));
    }

    private FeedbackVote parseVote(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("vote must be one of UP, DOWN, NONE");
        }
        try {
            return FeedbackVote.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("vote must be one of UP, DOWN, NONE: " + raw);
        }
    }

    private DownCategory parseDownCategory(String voteRaw, String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return null;
        }
        try {
            return DownCategory.valueOf(rawCategory.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("downCategory must be one of WRONG_ANSWER, TYPO, OFFENSIVE, OTHER: " + rawCategory);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private QuestionFeedbackResponse toResponse(QuestionFeedback f) {
        return new QuestionFeedbackResponse(
                f.getId(),
                f.getQuestionId(),
                f.getVote(),
                f.getDownCategory(),
                f.isReported(),
                f.getComment(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
