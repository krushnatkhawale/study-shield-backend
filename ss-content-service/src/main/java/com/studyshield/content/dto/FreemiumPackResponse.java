package com.studyshield.content.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FreemiumPackResponse(
        Long packId,
        String className,
        String language,
        String boardCode,
        List<String> subjects,
        int freemiumQuizzesPerSubject,
        int quizCount,
        String deviceId,
        Long childId,
        Long userId,
        List<QuizResponse> quizzes,
        LocalDateTime createdAt
) {}
