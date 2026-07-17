package com.studyshield.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChildProfileRequest(
    @NotBlank(message = "Name is required") String name,
    int age,
    @NotNull(message = "User ID is required") Long userId,
    Long boardId,
    Long classGradeId,
    boolean active
) {}
