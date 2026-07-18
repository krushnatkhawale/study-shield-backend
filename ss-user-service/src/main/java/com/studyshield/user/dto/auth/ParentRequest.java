package com.studyshield.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ParentRequest(
    String name,
    String gender,
    String relation,
    String type
) {}
