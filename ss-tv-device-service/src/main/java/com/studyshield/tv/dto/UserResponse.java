package com.studyshield.tv.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String externalId,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
