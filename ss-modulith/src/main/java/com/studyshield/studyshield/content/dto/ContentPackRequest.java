package com.studyshield.studyshield.content.dto;

import com.studyshield.studyshield.content.entity.ContentTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ContentPackRequest(
    @NotBlank(message = "Content pack name is required") String name,
    String description,
    @NotNull(message = "Subject ID is required") Long subjectId,
    int version,
    boolean active,
    ContentTier packType,
    LocalDate validFrom,
    LocalDate validTo
) {
    public ContentPackRequest {
        if (packType == null) {
            packType = ContentTier.FREEMIUM;
        }
    }
}
