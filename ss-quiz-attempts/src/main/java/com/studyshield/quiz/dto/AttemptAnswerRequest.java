package com.studyshield.quiz.dto;

import jakarta.validation.constraints.NotNull;

public record AttemptAnswerRequest(
    @NotNull Long questionId,
    String selectedOption
) {}
