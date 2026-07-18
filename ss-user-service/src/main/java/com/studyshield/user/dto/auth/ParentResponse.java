package com.studyshield.user.dto.auth;

public record ParentResponse(
    String parentId,
    String name
) {
    public static ParentResponse fromIdAndName(Long id, String name) {
        return new ParentResponse(id.toString(), name);
    }
}
