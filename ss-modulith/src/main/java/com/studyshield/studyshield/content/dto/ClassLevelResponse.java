package com.studyshield.studyshield.content.dto;

public record ClassLevelResponse(
        Long id,
        Integer ordinal,
        String slug,
        String canonicalName,
        Double ageMinYears,
        Double ageMaxYears,
        String stage,
        String notes
) {}
