package com.studyshield.studyshield.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    String name,
    String phone,
    String role,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
