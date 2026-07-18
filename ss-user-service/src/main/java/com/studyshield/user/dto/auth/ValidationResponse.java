package com.studyshield.user.dto.auth;

public record ValidationResponse(
    String accountId,
    String loginId,
    String parentId,
    String parentName,
    Boolean valid,
    String message,
    String errorCode,
    Long timestamp
) {
    public static ValidationResponse success(String accountId, String loginId, String parentName) {
        return new ValidationResponse(accountId, loginId, accountId, parentName, true, "Token is valid", null, System.currentTimeMillis());
    }

    public static ValidationResponse error(String errorCode, String message) {
        return new ValidationResponse(null, null, null, null, false, message, errorCode, System.currentTimeMillis());
    }
}
