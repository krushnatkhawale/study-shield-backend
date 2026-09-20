package com.studyshield.studyshield.studygoal.dto;

public record GoalProgressResponse(
    Long goalId,
    String type,
    Integer target,
    long current,
    boolean achieved
) {}
