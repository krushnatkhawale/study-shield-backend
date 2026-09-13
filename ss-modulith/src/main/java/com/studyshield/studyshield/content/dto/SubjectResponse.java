package com.studyshield.studyshield.content.dto;

import java.time.LocalDateTime;

public record SubjectResponse(
    Long id,
    String name,
    String code,
    String description,
    boolean active,
    int displayOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}