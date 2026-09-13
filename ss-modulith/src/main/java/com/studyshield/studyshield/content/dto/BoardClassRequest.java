package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BoardClassRequest(
        @NotNull(message = "Board ID is required") Long boardId,
        @NotNull(message = "Ordinal is required") Integer ordinal,
        @NotBlank(message = "Display name is required") String displayName
) {}