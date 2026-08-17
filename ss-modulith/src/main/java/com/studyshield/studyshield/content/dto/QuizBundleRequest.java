package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;

public record QuizBundleRequest(
        @NotBlank(message = "className is required") String className,
        String boardCode,
        String language,
        Long childId,
        String deviceId,
        Long userId,
        Boolean allowPartial
) {}
