package com.studyshield.quiz.dto;

import java.time.LocalDateTime;

public record AttemptAnswerResponse(
    Long id,
    Long quizAttemptId,
    Long questionId,
    String selectedOption,
    boolean correct,
    LocalDateTime createdAt
) {}
