package com.studyshield.studyshield.content.academicstructure.dto;

/**
 * One row of {@code academic-structure/board-classes.json}: a board's label for one
 * class level, referenced by {@code boardCode} + {@code classOrdinal}. Same schema
 * for load and export.
 */
public record BoardClassRow(
        Long id,
        String boardCode,
        int classOrdinal,
        String displayName
) {}
