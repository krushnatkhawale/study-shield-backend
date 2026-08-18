package com.studyshield.studyshield.quizresult.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizResultRequest(
    @NotBlank String childName,
    @NotNull @Min(0) Integer score,
    @NotNull @Min(1) Integer totalQuestions,
    @NotNull @Min(0) Long timeSpentSeconds,
    String contentName,
    String category,
    @NotNull Long completedAt
) {}
