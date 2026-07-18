package com.studyshield.user.dto.auth;

import com.studyshield.user.dto.ParentSummary;

import java.util.List;

public record AuthResponse(
    String accountId,
    String loginId,
    String sessionId,
    String parentId,
    String parentName,
    Boolean requiresParentSelection,
    List<ParentSummary> parents,
    String message,
    String errorCode,
    Long timestamp
) {
    public static AuthResponse success(String accountId, String loginId, String sessionId,
                                        String parentId, String parentName,
                                        boolean requiresParentSelection, List<ParentSummary> parents) {
        return new AuthResponse(accountId, loginId, sessionId, parentId, parentName,
                requiresParentSelection, parents, "Success", null, System.currentTimeMillis());
    }

    public static AuthResponse success(String accountId, String loginId, String sessionId,
                                        String parentId, String parentName) {
        return success(accountId, loginId, sessionId, parentId, parentName, false, List.of());
    }

    public static AuthResponse error(String errorCode, String message) {
        return new AuthResponse(null, null, null, null, null, null, null, message, errorCode, System.currentTimeMillis());
    }
}
