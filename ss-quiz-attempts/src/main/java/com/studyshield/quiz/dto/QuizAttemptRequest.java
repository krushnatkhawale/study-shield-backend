package com.studyshield.quiz.dto;

import jakarta.validation.constraints.NotNull;

public record QuizAttemptRequest(
    @NotNull Long quizId,
    @NotNull Long childProfileId,
    @NotNull Long userId,
    Integer totalQuestions
) {}
