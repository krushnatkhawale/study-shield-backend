package com.studyshield.user.dto.auth;

import com.studyshield.user.dto.ChildProfileResponse;

import java.time.Year;

public record StudentResponse(
    String studentId,
    String accountId,
    String name,
    String gender,
    Integer birthYear,
    String studentClass
) {
    public static StudentResponse fromChildProfile(ChildProfileResponse cp) {
        return new StudentResponse(
                cp.id().toString(),
                cp.userId().toString(),
                cp.name(),
                cp.gender(),
                cp.birthYear() != null ? cp.birthYear() : (cp.age() > 0 ? Year.now().getValue() - cp.age() : null),
                cp.studentClass()
        );
    }
}
