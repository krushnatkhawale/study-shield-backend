package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.ContentTier;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContentPackResponse(
    Long id,
    String name,
    String description,
    Long subjectId,
    String subjectName,
    int version,
    boolean active,
    ContentTier packType,
    LocalDate validFrom,
    LocalDate validTo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
