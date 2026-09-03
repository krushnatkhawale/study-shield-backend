package com.studyshield.studyshield.feedback.dto;

import com.studyshield.studyshield.feedback.entity.DownCategory;
import com.studyshield.studyshield.feedback.entity.FeedbackVote;

import java.time.LocalDateTime;

public record QuestionFeedbackResponse(
    Long id,
    Long questionId,
    FeedbackVote vote,
    DownCategory downCategory,
    boolean reported,
    String comment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
