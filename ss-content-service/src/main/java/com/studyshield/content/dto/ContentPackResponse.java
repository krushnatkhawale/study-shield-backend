package com.studyshield.content.dto;

import java.time.LocalDateTime;

public record ContentPackResponse(
    Long id,
    String name,
    String description,
    Long subjectId,
    String subjectName,
    int version,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
