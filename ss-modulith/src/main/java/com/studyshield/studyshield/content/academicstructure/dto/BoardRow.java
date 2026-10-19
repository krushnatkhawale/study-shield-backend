package com.studyshield.studyshield.content.academicstructure.dto;

/**
 * One row of {@code academic-structure/boards.json}. The parent country is referenced
 * by {@code countryIso2} (null for the generic ALL board and country-less boards).
 * Same schema for load and export.
 */
public record BoardRow(
        Long id,
        String countryIso2,
        String name,
        String code,
        String description,
        int minOrdinal,
        int maxOrdinal,
        Boolean active
) {}
