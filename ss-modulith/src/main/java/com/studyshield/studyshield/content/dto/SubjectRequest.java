package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
    @NotBlank(message = "Subject name is required") String name,
    @NotBlank(message = "Subject code is required") String code,
    String description,
    Long classGradeId,
    boolean active,
    int displayOrder
) {}
