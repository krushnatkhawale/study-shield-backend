package com.studyshield.studyshield.content.dto;

public record BoardClassResponse(
        Long id,
        Long boardId,
        Long classLevelId,
        String displayName,
        Integer ordinal,
        String boardCode,
        String boardName
) {}
