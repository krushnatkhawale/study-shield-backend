package com.studyshield.content.dto;

import com.studyshield.content.entity.ContentTier;
import com.studyshield.content.entity.Quiz.QuizType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quiz summary or detail. {@code questions} is populated on detail fetch (mobile download).
 */
public record QuizResponse(
        Long id,
        String title,
        String description,
        Long contentPackId,
        String contentPackName,
        QuizType quizType,
        int questionCount,
        ContentTier contentTier,
        Integer freemiumIndex,
        String language,
        boolean active,
        List<QuestionResponse> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
