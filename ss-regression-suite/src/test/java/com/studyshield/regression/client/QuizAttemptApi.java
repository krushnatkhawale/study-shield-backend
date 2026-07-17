package com.studyshield.regression.client;

import com.studyshield.regression.context.ScenarioContext;
import io.restassured.response.Response;

public class QuizAttemptApi {

    private final GatewayClient client;
    private final ScenarioContext context;

    public QuizAttemptApi(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    public Response createAttempt(String json) {
        return client.post("/api/v1/quiz-attempts", json);
    }

    public Response getAttempt(Long id) {
        return client.get("/api/v1/quiz-attempts/" + id);
    }

    public Response getAttemptsByUser(Long userId) {
        return client.get("/api/v1/quiz-attempts/user/" + userId);
    }

    public Response getAttemptsByChild(Long childId) {
        return client.get("/api/v1/quiz-attempts/child/" + childId);
    }

    public Response completeAttempt(Long id, int correctAnswers) {
        return client.put("/api/v1/quiz-attempts/" + id + "/complete?correctAnswers=" + correctAnswers);
    }

    public Response createAnswer(Long attemptId, String json) {
        return client.post("/api/v1/quiz-attempts/" + attemptId + "/answers", json);
    }

    public Response getAnswersByAttempt(Long attemptId) {
        return client.get("/api/v1/quiz-attempts/" + attemptId + "/answers");
    }

    public Response deleteAttempt(Long id) {
        return client.delete("/api/v1/quiz-attempts/" + id);
    }
}
