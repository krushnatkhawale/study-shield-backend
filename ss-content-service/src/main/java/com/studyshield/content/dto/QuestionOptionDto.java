package com.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Mirrors mobile asset option: {@code id}, {@code text}, {@code imageUrl}.
 */
public record QuestionOptionDto(
        @NotBlank(message = "Option id is required") String id,
        String text,
        String imageUrl
) {}
