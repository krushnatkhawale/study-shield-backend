package com.studyshield.tv.dto;

import java.time.LocalDateTime;

public record WifiNetworkResponse(
    Long id,
    String ssid,
    String bssid,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
