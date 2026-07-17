package com.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassGradeRequest(
    @NotNull(message = "Grade number is required") int gradeNumber,
    @NotBlank(message = "Name is required") String name,
    String description,
    @NotNull(message = "Board ID is required") Long boardId
) {}
