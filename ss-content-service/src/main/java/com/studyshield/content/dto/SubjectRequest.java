package com.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubjectRequest(
    @NotBlank(message = "Subject name is required") String name,
    @NotBlank(message = "Subject code is required") String code,
    String description,
    @NotNull(message = "ClassGrade ID is required") Long classGradeId,
    boolean active
) {}
