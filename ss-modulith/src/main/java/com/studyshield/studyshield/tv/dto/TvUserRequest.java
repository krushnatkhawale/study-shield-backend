package com.studyshield.studyshield.tv.dto;

import jakarta.validation.constraints.NotBlank;

public record TvUserRequest(
    @NotBlank String externalId,
    @NotBlank String name
) {}
