package com.studyshield.studyshield.user.dto.auth;

public record StudentRequest(
    String name,
    String gender,
    Integer birthYear,
    String studentClass,
    Boolean revealReadLock,
    Boolean autoDictation,
    Long fastAnswerThresholdMs,
    String greetingLanguage
) {}
