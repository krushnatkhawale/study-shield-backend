package com.studyshield.studyshield.user.dto;

import java.time.LocalDateTime;

public record ChildProfileResponse(
    Long id,
    String name,
    int age,
    Long userId,
    String userName,
    Long boardId,
    Long classGradeId,
    String gender,
    Integer birthYear,
    String studentClass,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
