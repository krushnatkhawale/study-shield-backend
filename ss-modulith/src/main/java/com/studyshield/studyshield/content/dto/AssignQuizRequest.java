package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotNull;

public record AssignQuizRequest(@NotNull Long quizId) {}
