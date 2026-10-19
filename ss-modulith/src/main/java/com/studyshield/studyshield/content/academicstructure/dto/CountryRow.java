package com.studyshield.studyshield.content.academicstructure.dto;

/**
 * One row of {@code academic-structure/countries.json}. Same schema for load and export.
 */
public record CountryRow(
        Long id,
        String iso2,
        String name
) {}
