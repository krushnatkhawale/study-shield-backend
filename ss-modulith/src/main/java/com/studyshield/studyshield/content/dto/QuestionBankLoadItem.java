package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Single question entry for the {@code POST /api/v1/questions/load} batch endpoint.
 * Each item carries enough metadata to create or find the full
 * Board → ClassGrade → Subject → ContentPack → Quiz → Question chain.
 *
 * <p>{@code questionType} is optional for backwards compatibility: clients that omit it
 * (e.g. the com.kaushalya.interrupter mobile seeder) get the type derived by
 * {@link com.studyshield.studyshield.content.service.QuestionBankLoader#resolveType}.
 */
public record QuestionBankLoadItem(
        @NotBlank(message = "boardCode is required") String boardCode,
        @NotBlank(message = "className is required") String className,
        Integer age,
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "questionText is required") String questionText,
        QuestionType questionType,
        List<String> options,
        @NotNull(message = "correctAnswer is required") String correctAnswer,
        String explanation,
        Integer orderIndex
) {}
