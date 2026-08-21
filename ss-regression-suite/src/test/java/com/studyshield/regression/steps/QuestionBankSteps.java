package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.ContentApi;
import com.studyshield.regression.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business-readable checks for the seeded question bank (issue #1):
 * sessions must serve real, age-appropriate questions — never placeholders, never empty.
 */
public class QuestionBankSteps {

    private final ContentApi contentApi;
    private final ScenarioContext context;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuestionBankSteps(ContentApi contentApi, ScenarioContext context) {
        this.contentApi = contentApi;
        this.context = context;
    }

    @Given("the question bank is seeded")
    public void theQuestionBankIsSeeded() {
        // Seeding happens lazily on first bundle request per class (QuizBundleSeeder).
    }

    @When("I request a quiz bundle for a child aged {int} with device id {string}")
    public void iRequestQuizBundleForAge(int age, String deviceId) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "age", age,
                "boardCode", "all",
                "language", "English",
                "deviceId", context.uniqueName(deviceId),
                "allowPartial", false
        ));
        issue(json);
    }

    @Then("every question in the bundle is a real curated question")
    public void everyQuestionIsReal() {
        Response response = context.getLastResponse();
        List<Map<String, Object>> quizzes = response.jsonPath().getList("quizzes");
        assertThat(quizzes).as("bundle should contain quizzes").isNotEmpty();

        int questionCount = 0;
        for (Map<String, Object> quiz : quizzes) {
            List<Map<String, Object>> questions = cast(quiz.get("questions"));
            assertThat(questions).as("quiz %s should have questions", quiz.get("id")).isNotEmpty();
            for (Map<String, Object> q : questions) {
                String text = String.valueOf(q.get("questionText"));
                assertThat(text)
                        .as("placeholder leaked into session: " + text)
                        .doesNotContain("Sample question", "Option A for");
                List<Object> options = (List<Object>) q.getOrDefault("options", List.of());
                assertThat(options.size()).as("question options: " + text).isGreaterThanOrEqualTo(2);
                questionCount++;
            }
        }
        assertThat(questionCount).as("total questions served in bundle").isGreaterThan(0);
    }

    private void issue(String json) {
        Response response = contentApi.issueQuizBundle(json);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long packId = response.jsonPath().getLong("packId");
            context.setCurrentQuizBundleId(packId);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> cast(Object value) {
        return value != null ? (List<Map<String, Object>>) value : List.of();
    }
}
