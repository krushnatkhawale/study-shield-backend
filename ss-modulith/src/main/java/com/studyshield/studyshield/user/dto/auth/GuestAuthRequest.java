package com.studyshield.studyshield.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for an anonymous (guest) session. The deviceId ties all guest
 * sessions on one installation to a single backend account, so quiz bundles
 * and quiz results persist per device across app restarts.
 */
public record GuestAuthRequest(
        @NotBlank(message = "deviceId is required") String deviceId
) {}