package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.ContentApi;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentSteps {

    private final ContentApi contentApi;
    private final ScenarioContext context;
    private final IdRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public ContentSteps(ContentApi contentApi, ScenarioContext context, IdRegistry registry) {
        this.contentApi = contentApi;
        this.context = context;
        this.registry = registry;
    }

    @Given("a board named {string} exists")
    public void aBoardNamedExists(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Created by regression suite",
                "active", true
        ));
        Response response = contentApi.createBoard(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentBoardId(id);
        registry.register("board", id);
    }

    @When("I create a board with name {string} and code {string}")
    public void iCreateBoardWithNameAndCode(String name, String code) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(code),
                "description", "Regression test",
                "active", true
        ));
        Response response = contentApi.createBoard(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentBoardId(id);
            registry.register("board", id);
        }
    }

    @When("I get board by id")
    public void iGetBoardById() {
        Response response = contentApi.getBoard(context.getCurrentBoardId());
        updateContext(response);
    }

    @When("I get all boards")
    public void iGetAllBoards() {
        Response response = contentApi.getAllBoards();
        updateContext(response);
    }

    @When("I delete the current board")
    public void iDeleteCurrentBoard() {
        Response response = contentApi.deleteBoard(context.getCurrentBoardId());
        updateContext(response);
    }

    @Given("a class grade {int} exists under current board")
    public void aClassGradeExistsUnderCurrentBoard(int gradeNumber) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "gradeNumber", gradeNumber,
                "name", "Class " + gradeNumber,
                "description", "Regression test",
                "boardId", context.getCurrentBoardId()
        ));
        Response response = contentApi.createClassGrade(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentClassGradeId(id);
        registry.register("class-grade", id);
    }

    @When("I create a class grade {int} under current board")
    public void iCreateClassGradeUnderCurrentBoard(int gradeNumber) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "gradeNumber", gradeNumber,
                "name", context.uniqueName("Class " + gradeNumber),
                "boardId", context.getCurrentBoardId(),
                "active", true
        ));
        Response response = contentApi.createClassGrade(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentClassGradeId(id);
            registry.register("class-grade", id);
        }
    }

    @Given("a subject {string} exists under current class grade")
    public void aSubjectExistsUnderCurrentClassGrade(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Regression test",
                "classGradeId", context.getCurrentClassGradeId(),
                "active", true
        ));
        Response response = contentApi.createSubject(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentSubjectId(id);
        registry.register("subject", id);
    }

    @Given("a content pack {string} exists under current subject")
    public void aContentPackExistsUnderCurrentSubject(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "description", "Regression test",
                "subjectId", context.getCurrentSubjectId(),
                "version", 1,
                "active", true
        ));
        Response response = contentApi.createContentPack(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentContentPackId(id);
        registry.register("content-pack", id);
    }

    @Given("a STANDARD quiz exists in current pack")
    public void aStandardQuizExistsInCurrentPack() throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "title", context.uniqueName("Standard Quiz"),
                "description", "Regression test",
                "contentPackId", context.getCurrentContentPackId(),
                "quizType", "STANDARD",
                "questionCount", 10,
                "active", true
        ));
        Response response = contentApi.createQuiz(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentQuizId(id);
        registry.register("quiz", id);
    }

    @When("I create a question with text {string}")
    public void iCreateAQuestionWithText(String questionText) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "questionText", questionText,
                "questionType", "SINGLE_CHOICE",
                "options", java.util.List.of(
                        Map.of("id", "A", "text", "Option A"),
                        Map.of("id", "B", "text", "Option B"),
                        Map.of("id", "C", "text", "Option C"),
                        Map.of("id", "D", "text", "Option D")
                ),
                "correctAnswers", java.util.List.of("A"),
                "quizId", context.getCurrentQuizId(),
                "blacklisted", false,
                "orderIndex", 0
        ));
        Response response = contentApi.createQuestion(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            registry.register("question", response.jsonPath().getLong("id"));
        }
    }

    @When("I get questions for current quiz")
    public void iGetQuestionsForCurrentQuiz() {
        Response response = contentApi.getQuestionsByQuiz(context.getCurrentQuizId());
        updateContext(response);
    }

    @When("I get active questions for current quiz")
    public void iGetActiveQuestionsForCurrentQuiz() {
        Response response = contentApi.getActiveQuestionsByQuiz(context.getCurrentQuizId());
        updateContext(response);
    }

    @When("I create a quiz with name {string} and code {string}")
    public void iCreateQuizWithNameAndCode(String title, String code) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "title", context.uniqueName(title),
                "description", "Regression test",
                "contentPackId", context.getCurrentContentPackId(),
                "quizType", "STANDARD",
                "questionCount", 10,
                "active", true
        ));
        Response response = contentApi.createQuiz(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentQuizId(id);
            registry.register("quiz", id);
        }
    }

    @When("I get quiz by id")
    public void iGetQuizById() {
        Response response = contentApi.getQuiz(context.getCurrentQuizId());
        updateContext(response);
    }

    @When("I request a freemium pack for class {string} with device id {string}")
    public void iRequestFreemiumPack(String className, String deviceId) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "className", className,
                "boardCode", "all",
                "language", "English",
                "deviceId", context.uniqueName(deviceId),
                "allowPartial", false
        ));
        Response response = contentApi.issueFreemiumPack(json);
        updateContext(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long packId = response.jsonPath().getLong("packId");
            context.setCurrentFreemiumPackId(packId);
            registry.register("freemium-pack", packId);
        }
    }

    @When("I get freemium pack by id")
    public void iGetFreemiumPackById() {
        Response response = contentApi.getFreemiumPack(context.getCurrentFreemiumPackId());
        updateContext(response);
    }

    @When("I request the same freemium pack for class {string} with device id {string}")
    public void iRequestSameFreemiumPack(String className, String deviceId) throws Exception {
        // Uses same unique prefix so deviceId matches prior request only if same string after uniqueName
        // For true idempotency test, store last device id — use pack re-fetch style with fixed device from context
        String json = mapper.writeValueAsString(Map.of(
                "className", className,
                "boardCode", "all",
                "language", "English",
                "deviceId", context.getSuitePrefix() + deviceId,
                "allowPartial", false
        ));
        Response response = contentApi.issueFreemiumPack(json);
        updateContext(response);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
