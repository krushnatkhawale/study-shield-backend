package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Single question entry for the {@code POST /api/v1/questions/load} batch endpoint.
 * Each item carries enough metadata to create or find the full
 * Board → ClassGrade → Subject → ContentPack → Quiz → Question chain.
 */
public record QuestionBankLoadItem(
        @NotBlank(message = "boardCode is required") String boardCode,
        @NotBlank(message = "className is required") String className,
        Integer age,
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "questionText is required") String questionText,
        @NotNull(message = "questionType is required") QuestionType questionType,
        List<String> options,
        @NotNull(message = "correctAnswer is required") String correctAnswer,
        String explanation,
        Integer orderIndex
) {}
