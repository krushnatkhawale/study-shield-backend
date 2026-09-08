package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.Difficulty;
import com.studyshield.studyshield.content.entity.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response aligned with mobile play payload (asset fields + server ids).
 * {@code versionGroupId} groups every version of the same question; {@code version} is the
 * ascending version number within that group (1 for the first version).
 */
public record QuestionResponse(
        Long id,
        String resourceId,
        String versionGroupId,
        int version,
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