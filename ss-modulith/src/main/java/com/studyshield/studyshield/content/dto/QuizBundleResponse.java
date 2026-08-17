package com.studyshield.studyshield.content.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuizBundleResponse(
        Long packId,
        String className,
        String language,
        String boardCode,
        List<String> subjects,
        int quizzesPerSubject,
        int quizCount,
        String deviceId,
        Long childId,
        Long userId,
        List<QuizResponse> quizzes,
        LocalDateTime createdAt
) {}
