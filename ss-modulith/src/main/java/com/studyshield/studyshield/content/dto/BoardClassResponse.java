package com.studyshield.studyshield.content.dto;

import java.time.LocalDateTime;

public record BoardClassResponse(
        Long id,
        Long boardId,
        String boardCode,
        String boardName,
        Long classLevelId,
        int ordinal,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}