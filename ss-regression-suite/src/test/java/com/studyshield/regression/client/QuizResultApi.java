package com.studyshield.regression.client;

import com.studyshield.regression.context.ScenarioContext;
import io.restassured.response.Response;

public class QuizResultApi {

    private final GatewayClient client;
    private final ScenarioContext context;

    public QuizResultApi(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    public Response saveResult(String json) {
        return client.post("/api/quiz-results", json);
    }

    public Response listResults() {
        return client.get("/api/quiz-results");
    }

    public ResultsResponse listResultsParsed() {
        Response response = listResults();
        return new ResultsResponse(response);
    }

    public Response listResultsByChild(String childName) {
        return client.get("/api/quiz-results/child/" + childName);
    }

    public static class ResultsResponse {
        private final Response response;

        public ResultsResponse(Response response) {
            this.response = response;
        }

        public int getStatusCode() {
            return response.getStatusCode();
        }

        public String getResultId() {
            return response.jsonPath().getString("resultId");
        }

        public String getMessage() {
            return response.jsonPath().getString("message");
        }
    }
}
