package com.studyshield.content.dto;

import java.time.LocalDateTime;

public record SubjectResponse(
    Long id,
    String name,
    String code,
    String description,
    Long classGradeId,
    String classGradeName,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
