package com.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardRequest(
    @NotBlank(message = "Board name is required")
    @Size(max = 255, message = "Board name must not exceed 255 characters")
    String name,

    @NotBlank(message = "Board code is required")
    @Size(max = 255, message = "Board code must not exceed 255 characters")
    String code,

    String description,
    boolean active
) {}
