package com.studyshield.studyshield.content.academicstructure.dto;

/**
 * One row of {@code academic-structure/offerings.json}: an offering is the combined
 * reference (board + class ordinal + global subject), referenced here by natural keys.
 * Same schema for load and export.
 */
public record OfferingRow(
        Long id,
        String boardCode,
        int classOrdinal,
        String subjectCode
) {}
