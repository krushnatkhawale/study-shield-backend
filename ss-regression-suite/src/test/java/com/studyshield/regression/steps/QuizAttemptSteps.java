package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.QuizAttemptApi;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QuizAttemptSteps {

    private final QuizAttemptApi quizAttemptApi;
    private final ScenarioContext context;
    private final IdRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuizAttemptSteps(QuizAttemptApi quizAttemptApi, ScenarioContext context, IdRegistry registry) {
        this.quizAttemptApi = quizAttemptApi;
        this.context = context;
        this.registry = registry;
    }

    @When("I start a quiz attempt for current user and child")
    public void iStartQuizAttemptForCurrentUserAndChild() throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "quizId", context.getCurrentQuizId(),
                "childProfileId", context.getCurrentChildId(),
                "userId", context.getCurrentUserId(),
                "totalQuestions", 10
        ));
        Response response = quizAttemptApi.createAttempt(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentAttemptId(id);
            registry.register("quiz-attempt", id);
        }
    }

    @When("I submit an answer for question {int}")
    public void iSubmitAnswerForQuestion(int questionIndex) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "questionId", (long) questionIndex,
                "selectedOption", "A"
        ));
        Response response = quizAttemptApi.createAnswer(context.getCurrentAttemptId(), json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            registry.register("attempt-answer", response.jsonPath().getLong("id"));
        }
    }

    @When("I complete the quiz attempt with {int} correct answers")
    public void iCompleteQuizAttemptWithCorrectAnswers(int correctAnswers) {
        Response response = quizAttemptApi.completeAttempt(context.getCurrentAttemptId(), correctAnswers);
        updateContext(response);
    }

    @When("I get attempts for current user")
    public void iGetAttemptsForCurrentUser() {
        Response response = quizAttemptApi.getAttemptsByUser(context.getCurrentUserId());
        updateContext(response);
    }

    @When("I get attempts for current child")
    public void iGetAttemptsForCurrentChild() {
        Response response = quizAttemptApi.getAttemptsByChild(context.getCurrentChildId());
        updateContext(response);
    }

    @Then("the attempt status should indicate completion")
    public void theAttemptStatusShouldIndicateCompletion() {
        assertThat(context.getLastResponseBody()).contains("COMPLETED");
    }

    @Then("the attempt result should include score fields")
    public void theAttemptResultShouldIncludeScoreFields() {
        assertThat(context.getLastResponseBody()).contains("score");
        assertThat(context.getLastResponseBody()).contains("correctAnswers");
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
