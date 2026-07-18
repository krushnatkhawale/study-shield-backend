package com.studyshield.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
    @NotBlank(message = "Login ID is required") String loginId,
    @NotBlank(message = "Password is required") String password,
    String name
) {}
