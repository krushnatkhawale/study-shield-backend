package com.studyshield.studyshield.studygoal.dto;

import java.time.LocalDateTime;

public record StudyGoalResponse(
    Long id,
    Long accountId,
    String childName,
    String type,
    Integer target,
    boolean active,
    LocalDateTime createdAt
) {}
