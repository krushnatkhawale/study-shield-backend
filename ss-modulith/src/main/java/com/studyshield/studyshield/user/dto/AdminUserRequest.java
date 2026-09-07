package com.studyshield.studyshield.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for managing ADMIN-type accounts ({@code /api/v1/admin-users}).
 * The type/role are fixed to ADMIN by the controller; password is write-only
 * (set at create, optionally reset on update, never returned).
 */
public record AdminUserRequest(
    @NotBlank(message = "Email is required") String email,
    String password,
    @NotBlank(message = "Name is required") String name,
    String phone,
    boolean active
) {}