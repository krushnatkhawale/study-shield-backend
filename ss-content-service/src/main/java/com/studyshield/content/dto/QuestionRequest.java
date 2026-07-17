package com.studyshield.content.dto;

import com.studyshield.content.entity.Difficulty;
import com.studyshield.content.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body aligned with mobile {@code QuizFileQuestion} / assets JSON.
 */
public record QuestionRequest(
        String resourceId,
        @NotBlank(message = "Question text is required") String questionText,
        String questionImageUrl,
        @NotNull(message = "Question type is required") QuestionType questionType,
        @Valid List<QuestionOptionDto> options,
        @NotEmpty(message = "At least one correct answer is required") List<String> correctAnswers,
        String explanation,
        Integer points,
        Difficulty difficulty,
        List<String> languages,
        List<String> tags,
        @NotNull(message = "Quiz ID is required") Long quizId,
        boolean blacklisted,
        int orderIndex
) {}
