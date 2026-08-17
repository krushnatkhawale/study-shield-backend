package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassGradeRequest(
    @NotBlank(message = "Name is required") String name,
    String description,
    @NotNull(message = "Board ID is required") Long boardId
) {}
