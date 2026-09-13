package com.studyshield.studyshield.content.dto;

import java.time.LocalDateTime;

public record BoardClassSubjectResponse(
        Long id,
        Long boardClassId,
        String boardCode,
        int ordinal,
        String className,
        Long subjectId,
        String subjectCode,
        String subjectName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}