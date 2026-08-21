package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.AuthApi;
import com.studyshield.regression.client.QuizResultApi;
import com.studyshield.regression.context.AuthContext;
import com.studyshield.regression.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QuizResultSteps {

    private static final String TEST_PASSWORD = "Test1234!";

    private final QuizResultApi quizResultApi;
    private final AuthApi authApi;
    private final AuthContext authContext;
    private final ScenarioContext context;
    private final ObjectMapper mapper = new ObjectMapper();
    private String tokenBeforeSecondUser;

    public QuizResultSteps(QuizResultApi quizResultApi, AuthApi authApi,
                           AuthContext authContext, ScenarioContext context) {
        this.quizResultApi = quizResultApi;
        this.authApi = authApi;
        this.authContext = authContext;
        this.context = context;
    }

    @When("I save a quiz result for child {string} with score {int} and {int} total questions")
    public void iSaveQuizResultForChildWithScore(String childName, int score, int totalQuestions) throws Exception {
        long now = System.currentTimeMillis();
        String json = mapper.writeValueAsString(Map.of(
                "childName", childName,
                "score", score,
                "totalQuestions", totalQuestions,
                "timeSpentSeconds", 120L,
                "contentName", "Test Quiz",
                "category", "Math",
                "completedAt", now
        ));
        Response response = quizResultApi.saveResult(json);
        updateContext(response);
    }

    @When("I save a quiz result for child {string} with score {int}, {int} total questions, and content {string}")
    public void iSaveQuizResultForChildWithScoreAndContent(String childName, int score, int totalQuestions, String contentName) throws Exception {
        long now = System.currentTimeMillis();
        String json = mapper.writeValueAsString(Map.of(
                "childName", childName,
                "score", score,
                "totalQuestions", totalQuestions,
                "timeSpentSeconds", 90L,
                "contentName", contentName,
                "category", "Science",
                "completedAt", now
        ));
        Response response = quizResultApi.saveResult(json);
        updateContext(response);
    }

    @When("I save a quiz result with missing child name")
    public void iSaveQuizResultWithMissingChildName() throws Exception {
        long now = System.currentTimeMillis();
        String json = mapper.writeValueAsString(Map.of(
                "score", 5,
                "totalQuestions", 10,
                "timeSpentSeconds", 60L,
                "completedAt", now
        ));
        Response response = quizResultApi.saveResult(json);
        updateContext(response);
    }

    @When("I save a quiz result with negative score")
    public void iSaveQuizResultWithNegativeScore() throws Exception {
        long now = System.currentTimeMillis();
        String json = mapper.writeValueAsString(Map.of(
                "childName", "Test Child",
                "score", -1,
                "totalQuestions", 10,
                "timeSpentSeconds", 60L,
                "completedAt", now
        ));
        Response response = quizResultApi.saveResult(json);
        updateContext(response);
    }

    @When("I list all quiz results")
    public void iListAllQuizResults() {
        Response response = quizResultApi.listResults();
        updateContext(response);
    }

    @When("I list quiz results for child {string}")
    public void iListQuizResultsForChild(String childName) {
        Response response = quizResultApi.listResultsByChild(childName);
        updateContext(response);
    }

    @Then("the quiz result response should contain resultId")
    public void theQuizResultResponseShouldContainResultId() {
        Response response = context.getLastResponse();
        String resultId = response.jsonPath().getString("resultId");
        assertThat(resultId)
                .as("Response should contain a non-null resultId")
                .isNotNull();
    }

    @Then("the quiz result response message should be {string}")
    public void theQuizResultResponseMessageShouldBe(String expectedMessage) {
        Response response = context.getLastResponse();
        String message = response.jsonPath().getString("message");
        assertThat(message).isEqualTo(expectedMessage);
    }

    @Then("the quiz result response should not contain errorCode")
    public void theQuizResultResponseShouldNotContainErrorCode() {
        Response response = context.getLastResponse();
        String errorCode = response.jsonPath().getString("errorCode");
        assertThat(errorCode).isNull();
    }

    @Then("the quiz result list should contain at least {int} result\\(s)")
    public void theQuizResultListShouldContainAtLeastResults(int minSize) {
        Response response = context.getLastResponse();
        java.util.List<?> results = response.jsonPath().getList("$");
        assertThat(results).hasSizeGreaterThanOrEqualTo(minSize);
    }

    @Then("the quiz result list item should have expected fields")
    public void theQuizResultListItemShouldHaveExpectedFields() {
        Response response = context.getLastResponse();
        assertThat(response.jsonPath().getString("childName")).isNotNull();
        assertThat((Object) response.jsonPath().get("score")).isNotNull();
        assertThat((Object) response.jsonPath().get("totalQuestions")).isNotNull();
        assertThat((Object) response.jsonPath().get("timeSpentSeconds")).isNotNull();
        assertThat(response.jsonPath().getString("completedAt")).isNotNull();
    }

    @Then("the quiz result list should contain child {string}")
    public void theQuizResultListShouldContainChild(String childName) {
        Response response = context.getLastResponse();
        java.util.List<String> childNames = response.jsonPath().getList("childName");
        assertThat(childNames).contains(childName);
    }

    @When("another parent user signs up")
    public void anotherParentUserSignsUp() {
        tokenBeforeSecondUser = authContext.getJwtToken();
        String email = context.uniqueName("second_parent@test.com");
        Response response = authApi.signUp(email, TEST_PASSWORD, context.uniqueName("Second Parent"));
        assertThat(response.getStatusCode())
                .as("Second parent signup failed: {}", response.getBody().asString())
                .isEqualTo(200);
        authContext.setJwtToken(response.jsonPath().getString("sessionId"));
    }

    @When("I switch back to the original parent user")
    public void iSwitchBackToTheOriginalParentUser() {
        assertThat(tokenBeforeSecondUser).isNotNull();
        authContext.setJwtToken(tokenBeforeSecondUser);
    }

    @Then("the quiz result list should not contain child {string}")
    public void theQuizResultListShouldNotContainChild(String childName) {
        Response response = context.getLastResponse();
        java.util.List<String> childNames = response.jsonPath().getList("childName");
        assertThat(childNames == null ? java.util.List.<String>of() : childNames)
                .as("Results of another account must not be visible")
                .doesNotContain(childName);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
