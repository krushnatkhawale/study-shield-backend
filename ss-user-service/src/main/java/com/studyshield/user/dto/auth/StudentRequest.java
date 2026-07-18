package com.studyshield.user.dto.auth;

public record StudentRequest(
    String name,
    String gender,
    Integer birthYear,
    String studentClass
) {}
