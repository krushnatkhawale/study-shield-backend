package com.studyshield.studyshield.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ClaimGuestDataRequest(
        @NotBlank(message = "deviceId is required") String deviceId
) {}
