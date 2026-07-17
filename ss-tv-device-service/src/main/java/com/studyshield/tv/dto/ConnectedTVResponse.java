package com.studyshield.tv.dto;

import java.time.LocalDateTime;

public record ConnectedTVResponse(
    Long id,
    String deviceName,
    String macAddress,
    String ipAddress,
    Long wifiNetworkId,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
