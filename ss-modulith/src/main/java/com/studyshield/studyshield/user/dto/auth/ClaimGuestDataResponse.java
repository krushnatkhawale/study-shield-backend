package com.studyshield.studyshield.user.dto.auth;

public record ClaimGuestDataResponse(
        boolean success,
        int resultsMoved,
        int childrenMoved,
        int attemptsMoved,
        String message,
        String errorCode
) {
    public static ClaimGuestDataResponse success(int resultsMoved, int childrenMoved, int attemptsMoved) {
        return new ClaimGuestDataResponse(true, resultsMoved, childrenMoved, attemptsMoved, "Guest data migrated", null);
    }

    public static ClaimGuestDataResponse error(String errorCode, String message) {
        return new ClaimGuestDataResponse(false, 0, 0, 0, message, errorCode);
    }
}
