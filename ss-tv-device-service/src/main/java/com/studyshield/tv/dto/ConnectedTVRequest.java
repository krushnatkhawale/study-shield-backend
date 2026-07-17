package com.studyshield.tv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConnectedTVRequest(
    @NotBlank String deviceName,
    @NotBlank String macAddress,
    String ipAddress,
    @NotNull Long wifiNetworkId,
    boolean active
) {}
