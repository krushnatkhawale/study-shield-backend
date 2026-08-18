package com.studyshield.studyshield.quizresult.dto;

import java.time.LocalDateTime;

public record QuizResultListItem(
    Long id,
    String childName,
    Integer score,
    Integer totalQuestions,
    Long timeSpentSeconds,
    String contentName,
    String category,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {}
