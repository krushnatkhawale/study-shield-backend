package com.studyshield.content.dto;

import java.time.LocalDateTime;

public record ClassGradeResponse(
    Long id,
    int gradeNumber,
    String name,
    String description,
    Long boardId,
    String boardName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
