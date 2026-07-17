package com.studyshield.user.dto;

import java.time.LocalDateTime;

public record ChildProfileResponse(
    Long id,
    String name,
    int age,
    Long userId,
    String userName,
    Long boardId,
    Long classGradeId,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
