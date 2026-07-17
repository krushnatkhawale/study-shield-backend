package com.studyshield.content.dto;

import com.studyshield.content.entity.ContentTier;
import com.studyshield.content.entity.Quiz.QuizType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizRequest(
        @NotBlank(message = "Quiz title is required") String title,
        String description,
        @NotNull(message = "Content pack ID is required") Long contentPackId,
        QuizType quizType,
        Integer questionCount,
        ContentTier contentTier,
        Integer freemiumIndex,
        String language,
        boolean active
) {}
