package com.studyshield.regression.client;

import com.studyshield.regression.context.ScenarioContext;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentApi {

    private static final Logger log = LoggerFactory.getLogger(ContentApi.class);
    private final GatewayClient client;
    private final ScenarioContext context;

    public ContentApi(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    public Response createBoard(String json) {
        return client.post("/api/v1/boards", json);
    }

    public Response getBoard(Long id) {
        return client.get("/api/v1/boards/" + id);
    }

    public Response getAllBoards() {
        return client.get("/api/v1/boards");
    }

    public Response updateBoard(Long id, String json) {
        return client.put("/api/v1/boards/" + id, json);
    }

    public Response deleteBoard(Long id) {
        return client.delete("/api/v1/boards/" + id);
    }

    public Response createBoardClass(String json) {
        return client.post("/api/v1/board-classes", json);
    }

    public Response getBoardClass(Long id) {
        return client.get("/api/v1/board-classes/" + id);
    }

    public Response createSubject(String json) {
        return client.post("/api/v1/subjects", json);
    }

    public Response updateSubject(Long id, String json) {
        return client.put("/api/v1/subjects/" + id, json);
    }

    public Response createContentPack(String json) {
        return client.post("/api/v1/content-packs", json);
    }

    public Response getClassLevelsByOrdinal(int ordinal) {
        return client.get("/api/v1/class-levels/ordinal/" + ordinal);
    }

    public Response getBoardClassesByBoard(Long boardId) {
        return client.get("/api/v1/board-classes/board/" + boardId);
    }

    /**
     * Academic catalog is offering-based (board + class ordinal + global subject).
     * Class-grade endpoints are gone; subjects are global; packs hang off offerings.
     */
    public Response createOffering(String json) {
        return client.post("/api/v1/offerings", json);
    }

    public Response getOffering(Long id) {
        return client.get("/api/v1/offerings/" + id);
    }

    public Response getOfferingsByBoardClass(Long boardClassId) {
        return client.get("/api/v1/offerings/board-class/" + boardClassId);
    }

    public Response getSubjects() {
        return client.get("/api/v1/subjects");
    }

    public Response getContentPacksByOffering(Long offeringId) {
        return client.get("/api/v1/content-packs/offering/" + offeringId);
    }

    public Response getClassLevels() {
        return client.get("/api/v1/class-levels");
    }

    public Response createQuiz(String json) {
        return client.post("/api/v1/quizzes", json);
    }

    public Response getQuiz(Long id) {
        return client.get("/api/v1/quizzes/" + id);
    }

    public Response getQuizzesByContentPack(Long contentPackId) {
        return client.get("/api/v1/quizzes/content-pack/" + contentPackId);
    }

    public Response createQuestion(String json) {
        return client.post("/api/v1/questions", json);
    }

    public Response issueQuizBundle(String json) {
        return client.post("/api/v1/quiz-bundles", json);
    }

    public Response getQuizBundle(Long packId) {
        return client.get("/api/v1/quiz-bundles/" + packId);
    }

    public Response getQuestionsByQuiz(Long quizId) {
        return client.get("/api/v1/questions/quiz/" + quizId);
    }

    public Response getActiveQuestionsByQuiz(Long quizId) {
        return client.get("/api/v1/questions/quiz/" + quizId + "/active");
    }

    public Response deleteQuestion(Long id) {
        return client.delete("/api/v1/questions/" + id);
    }

    public Response deleteQuiz(Long id) {
        return client.delete("/api/v1/quizzes/" + id);
    }

    public Response deleteContentPack(Long id) {
        return client.delete("/api/v1/content-packs/" + id);
    }

    public Response deleteSubject(Long id) {
        return client.delete("/api/v1/subjects/" + id);
    }
}
