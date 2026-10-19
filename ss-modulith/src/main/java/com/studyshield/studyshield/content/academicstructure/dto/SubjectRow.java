package com.studyshield.studyshield.content.academicstructure.dto;

/**
 * One row of {@code academic-structure/subjects.json}. Same schema for load and export.
 */
public record SubjectRow(
        Long id,
        String code,
        String name,
        String description,
        Boolean active,
        Integer displayOrder
) {}
