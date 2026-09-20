package com.studyshield.studyshield.content.dto;

public record OfferingResponse(
        Long id,
        Long boardClassId,
        Long subjectId,
        String displayName,
        Integer ordinal,
        String boardCode,
        String subjectCode,
        String subjectName
) {}
