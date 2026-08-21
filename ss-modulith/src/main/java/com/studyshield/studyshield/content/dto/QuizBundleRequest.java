package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Bundle request. Either className or age must be supplied — when className is blank the
 * class is derived from age so sessions stay age-appropriate.
 */
public record QuizBundleRequest(
        String className,
        Integer age,
        String boardCode,
        String language,
        Long childId,
        String deviceId,
        Long userId,
        Boolean allowPartial
) {}
