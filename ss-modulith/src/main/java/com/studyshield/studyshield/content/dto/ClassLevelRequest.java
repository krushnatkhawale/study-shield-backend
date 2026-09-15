package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassLevelRequest(
        @NotNull Integer ordinal,
        @NotBlank String slug,
        @NotBlank String canonicalName,
        Double ageMinYears,
        Double ageMaxYears,
        String stage,
        String notes
) {}
