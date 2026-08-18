package com.studyshield.studyshield.quizresult.dto;

public record QuizResultResponse(
    String resultId,
    String message,
    String errorCode
) {
    public static QuizResultResponse success(String resultId) {
        return new QuizResultResponse(resultId, "Quiz result saved successfully", null);
    }

    public static QuizResultResponse error(String errorCode, String message) {
        return new QuizResultResponse(null, message, errorCode);
    }
}
