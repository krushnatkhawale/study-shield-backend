package com.studyshield.quiz.dto;

import java.time.LocalDateTime;

public record QuizAttemptResponse(
    Long id,
    Long quizId,
    Long childProfileId,
    Long userId,
    String status,
    Integer totalQuestions,
    Integer correctAnswers,
    Integer score,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
