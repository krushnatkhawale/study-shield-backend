package com.studyshield.studyshield.content.dto;

public record BoardClassResponse(
        Long id,
        Long boardId,
        Long classLevelId,
        String displayName
) {}
