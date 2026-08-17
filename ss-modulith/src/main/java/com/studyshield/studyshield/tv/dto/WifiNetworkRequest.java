package com.studyshield.studyshield.tv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WifiNetworkRequest(
    @NotBlank String ssid,
    String bssid,
    @NotNull Long userId
) {}
