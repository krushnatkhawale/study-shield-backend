package com.studyshield.studyshield.content.dto;

import jakarta.validation.constraints.NotBlank;

public record OfferingRequest(
        String boardCode,
        @NotBlank(message = "className is required") String className,
        @NotBlank(message = "subject is required") String subject
) {}