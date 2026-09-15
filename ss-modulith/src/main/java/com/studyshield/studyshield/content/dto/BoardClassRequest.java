package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardClassRequest(
        @NotNull Long boardId,
        @NotNull Long classLevelId,
        @NotBlank String displayName
) {}
