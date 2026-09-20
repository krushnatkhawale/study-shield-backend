package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardClassRequest(
        @NotNull Long boardId,
        Long classLevelId,
        Integer ordinal,
        @NotBlank String displayName
) {
    @AssertTrue(message = "classLevelId or ordinal is required")
    public boolean isLevelPresent() {
        return classLevelId != null || ordinal != null;
    }
}
