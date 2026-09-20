package com.studyshield.studyshield.studygoal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record StudyGoalRequest(
    @NotBlank String childName,
    @NotBlank @Pattern(regexp = "WEEKLY_QUIZZES|WEEKLY_BEST") String type,
    @NotNull @Min(1) Integer target,
    Boolean active
) {}
