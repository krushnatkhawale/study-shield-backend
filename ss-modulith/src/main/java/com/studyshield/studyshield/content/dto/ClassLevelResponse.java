package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.ClassLevel.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClassLevelResponse(
        Long id,
        int ordinal,
        String slug,
        String canonicalName,
        BigDecimal ageMinYears,
        BigDecimal ageMaxYears,
        Stage stage,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}