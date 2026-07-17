package com.studyshield.content.dto;

import java.time.LocalDateTime;

public record BoardResponse(
    Long id,
    String name,
    String code,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
