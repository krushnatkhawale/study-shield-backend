package com.studyshield.regression.fixtures;

import com.studyshield.regression.context.ScenarioContext;

import java.util.Map;

public class PayloadFactory {

    private final ScenarioContext context;

    public PayloadFactory(ScenarioContext context) {
        this.context = context;
    }

    public Map<String, Object> board(String name) {
        return Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Regression test fixture",
                "active", true
        );
    }

    public Map<String, Object> classGrade(int gradeNumber, Long boardId) {
        return Map.of(
                "gradeNumber", gradeNumber,
                "name", context.uniqueName("Class " + gradeNumber),
                "description", "Regression test fixture",
                "boardId", boardId,
                "active", true
        );
    }

    public Map<String, Object> subject(String name, Long classGradeId) {
        return Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Regression test fixture",
                "classGradeId", classGradeId,
                "active", true
        );
    }

    public Map<String, Object> contentPack(String name, Long subjectId) {
        return Map.of(
                "name", context.uniqueName(name),
                "description", "Regression test fixture",
                "subjectId", subjectId,
                "version", 1,
                "active", true
        );
    }

    public Map<String, Object> quiz(String title, Long contentPackId, String quizType) {
        return Map.of(
                "title", context.uniqueName(title),
                "description", "Regression test fixture",
                "contentPackId", contentPackId,
                "quizType", quizType,
                "questionCount", 10,
                "active", true
        );
    }

    public Map<String, Object> question(String text, Long quizId, int orderIndex) {
        return Map.of(
                "questionText", text,
                "optionA", "Option A",
                "optionB", "Option B",
                "optionC", "Option C",
                "optionD", "Option D",
                "correctOption", "A",
                "quizId", quizId,
                "blacklisted", false,
                "orderIndex", orderIndex
        );
    }

    public Map<String, Object> user(String name, String role) {
        return Map.of(
                "email", context.uniqueName(name.toLowerCase().replace(" ", "_") + "@test.com"),
                "name", context.uniqueName(name),
                "phone", "9876543210",
                "role", role,
                "active", true
        );
    }

    public Map<String, Object> child(String name, Long userId) {
        return Map.of(
                "name", context.uniqueName(name),
                "age", 8,
                "userId", userId,
                "active", true
        );
    }

    public Map<String, Object> wifiNetwork(String ssid, Long userId) {
        return Map.of(
                "ssid", context.uniqueName(ssid),
                "bssid", "AA:BB:CC:DD:EE:FF",
                "userId", userId
        );
    }

    public Map<String, Object> connectedTv(String deviceName, Long wifiNetworkId) {
        return Map.of(
                "deviceName", context.uniqueName(deviceName),
                "macAddress", "11:22:33:44:55:66",
                "ipAddress", "192.168.1.100",
                "wifiNetworkId", wifiNetworkId,
                "active", true
        );
    }

    public Map<String, Object> quizAttempt(Long quizId, Long childProfileId, Long userId) {
        return Map.of(
                "quizId", quizId,
                "childProfileId", childProfileId,
                "userId", userId,
                "totalQuestions", 10
        );
    }

    public Map<String, Object> attemptAnswer(Long questionId, String selectedOption) {
        return Map.of(
                "questionId", questionId,
                "selectedOption", selectedOption
        );
    }
}
