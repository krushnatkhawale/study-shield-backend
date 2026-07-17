package com.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentPackRequest(
    @NotBlank(message = "Content pack name is required") String name,
    String description,
    @NotNull(message = "Subject ID is required") Long subjectId,
    int version,
    boolean active
) {}
