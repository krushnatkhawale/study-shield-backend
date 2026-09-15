package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotNull;

public record OfferingRequest(
        @NotNull Long boardClassId,
        @NotNull Long subjectId
) {}
