package com.studyshield.studyshield.content.academicstructure.dto;

import java.math.BigDecimal;

/**
 * One row of {@code academic-structure/class-levels.json}. The same schema is used
 * by load (GitHub file or upload) and export — the round-trip invariant.
 * {@code id} is preserved so {@code load(export(x))} is exact.
 */
public record ClassLevelRow(
        Long id,
        int ordinal,
        String slug,
        String canonicalName,
        BigDecimal ageMinYears,
        BigDecimal ageMaxYears,
        String stage,
        String notes
) {}
