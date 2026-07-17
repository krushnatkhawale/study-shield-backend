package com.studyshield.tv.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank String externalId,
    @NotBlank String name
) {}
