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
        String code = context.uniqueName(name.toUpperCase().replace(" ", "_"));
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", code,
                "description", "Created by regression suite",
                "active", true
        ));
        Response response = contentApi.createBoard(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentBoardId(id);
        context.setCurrentBoardCode(code);
        registry.register("board", id);
    }

    @When("I create a board with name {string} and code {string}")
    public void iCreateBoardWithNameAndCode(String name, String code) throws Exception {
        String uniqueCode = context.uniqueName(code);
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", uniqueCode,
                "description", "Regression test",
                "active", true
        ));
        Response response = contentApi.createBoard(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentBoardId(id);
            context.setCurrentBoardCode(uniqueCode);
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

    @When("I load the board with code {string}")
    public void iLoadBoardWithCode(String code) {
        Response response = contentApi.getAllBoards();
        assertThat(response.getStatusCode()).isEqualTo(200);
        java.util.List<Map<String, Object>> boards = response.jsonPath().getList("$");
        Map<String, Object> match = boards.stream()
                .filter(b -> code.equals(String.valueOf(b.get("code"))))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seeded board " + code + " not found"));
        context.setCurrentBoardId(((Number) match.get("id")).longValue());
        context.setCurrentBoardCode(code);
        updateContext(response);
    }

    @When("I get board classes for current board")
    public void iGetBoardClassesForCurrentBoard() {
        Response response = contentApi.getBoardClassesByBoard(context.getCurrentBoardId());
        updateContext(response);
    }

    @When("I get offerings for current board class")
    public void iGetOfferingsForCurrentBoardClass() {
        Response response = contentApi.getOfferingsByBoardClass(context.getCurrentBoardClassId());
        updateContext(response);
    }

    @When("I delete the current board")
    public void iDeleteCurrentBoard() {
        Response response = contentApi.deleteBoard(context.getCurrentBoardId());
        updateContext(response);
    }

    @Given("a board class {string} exists at ordinal {int} under current board")
    public void aBoardClassExistsUnderCurrentBoard(String displayName, int ordinal) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "boardId", context.getCurrentBoardId(),
                "ordinal", ordinal,
                "displayName", displayName
        ));
        Response response = contentApi.createBoardClass(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        context.setCurrentBoardClassId(response.jsonPath().getLong("id"));
    }

    @When("I create a board class {string} at ordinal {int} under current board")
    public void iCreateBoardClassUnderCurrentBoard(String displayName, int ordinal) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "boardId", context.getCurrentBoardId(),
                "ordinal", ordinal,
                "displayName", displayName
        ));
        Response response = contentApi.createBoardClass(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            context.setCurrentBoardClassId(response.jsonPath().getLong("id"));
        }
    }

    @Given("an offering for subject {string} exists under current board class")
    public void anOfferingForSubjectExistsUnderCurrentBoardClass(String subject) throws Exception {
        Response boardClassResponse = contentApi.getBoardClass(context.getCurrentBoardClassId());
        assertThat(boardClassResponse.getStatusCode()).isEqualTo(200);
        String json = mapper.writeValueAsString(Map.of(
                "boardCode", boardClassResponse.jsonPath().getString("boardCode"),
                "className", boardClassResponse.jsonPath().getString("displayName"),
                "subject", subject
        ));
        Response response = contentApi.createOffering(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        context.setCurrentOfferingId(response.jsonPath().getLong("id"));
    }

    @Given("a global subject {string} exists")
    public void aGlobalSubjectExists(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Regression test",
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

    @When("I create a subject with name {string}")
    public void iCreateSubjectWithName(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "code", context.uniqueName(name.toUpperCase().replace(" ", "_")),
                "description", "Regression test",
                "active", true
        ));
        Response response = contentApi.createSubject(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentSubjectId(id);
            registry.register("subject", id);
        }
    }

    @When("I get all subjects")
    public void iGetAllSubjects() {
        Response response = contentApi.getSubjects();
        updateContext(response);
    }

    @Given("a content pack {string} exists under current offering")
    public void aContentPackExistsUnderCurrentOffering(String name) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(name),
                "description", "Regression test",
                "offeringId", context.getCurrentOfferingId(),
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

    @When("I request a quiz bundle for class {string} with device id {string}")
    public void iRequestQuizBundle(String className, String deviceId) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "className", className,
                "boardCode", "all",
                "language", "English",
                "deviceId", context.uniqueName(deviceId),
                "allowPartial", false
        ));
        Response response = contentApi.issueQuizBundle(json);
        updateContext(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long packId = response.jsonPath().getLong("packId");
            context.setCurrentQuizBundleId(packId);
            registry.register("quiz-bundle", packId);
        }
    }

    @When("I get quiz bundle by id")
    public void iGetQuizBundleById() {
        Response response = contentApi.getQuizBundle(context.getCurrentQuizBundleId());
        updateContext(response);
    }

    @When("I request the same quiz bundle for class {string} with device id {string}")
    public void iRequestSameQuizBundle(String className, String deviceId) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "className", className,
                "boardCode", "all",
                "language", "English",
                "deviceId", context.getSuitePrefix() + deviceId,
                "allowPartial", false
        ));
        Response response = contentApi.issueQuizBundle(json);
        updateContext(response);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
