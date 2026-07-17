package com.studyshield.content.dto;

import com.studyshield.content.entity.Difficulty;
import com.studyshield.content.entity.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response aligned with mobile play payload (asset fields + server ids).
 */
public record QuestionResponse(
        Long id,
        String resourceId,
        String questionText,
        String questionImageUrl,
        QuestionType questionType,
        List<QuestionOptionDto> options,
        List<String> correctAnswers,
        String explanation,
        int points,
        Difficulty difficulty,
        List<String> languages,
        List<String> tags,
        Long quizId,
        boolean blacklisted,
        int orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
