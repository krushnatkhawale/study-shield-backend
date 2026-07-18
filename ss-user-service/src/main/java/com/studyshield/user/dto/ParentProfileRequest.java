package com.studyshield.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ParentProfileRequest(
    @NotBlank(message = "Name is required") String name,
    String gender,
    String relation,
    String type
) {}
