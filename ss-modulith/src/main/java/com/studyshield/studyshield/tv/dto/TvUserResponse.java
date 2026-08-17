package com.studyshield.studyshield.tv.dto;

import java.time.LocalDateTime;

public record TvUserResponse(
    Long id,
    String externalId,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
