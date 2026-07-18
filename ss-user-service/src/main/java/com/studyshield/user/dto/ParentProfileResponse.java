package com.studyshield.user.dto;

import java.time.LocalDateTime;

public record ParentProfileResponse(
    Long id,
    Long userId,
    String name,
    String gender,
    String relation,
    String type,
    boolean isDefault,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
