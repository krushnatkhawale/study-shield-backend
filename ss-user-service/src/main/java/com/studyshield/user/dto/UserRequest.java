package com.studyshield.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "Name is required") String name,
    String phone,
    String role,
    boolean active
) {}
