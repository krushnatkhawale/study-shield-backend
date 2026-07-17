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

    public Response createClassGrade(String json) {
        return client.post("/api/v1/class-grades", json);
    }

    public Response getClassGradesByBoard(Long boardId) {
        return client.get("/api/v1/class-grades/board/" + boardId);
    }

    public Response createSubject(String json) {
        return client.post("/api/v1/subjects", json);
    }

    public Response getSubjectsByClassGrade(Long classGradeId) {
        return client.get("/api/v1/subjects/class-grade/" + classGradeId);
    }

    public Response createContentPack(String json) {
        return client.post("/api/v1/content-packs", json);
    }

    public Response getContentPacksBySubject(Long subjectId) {
        return client.get("/api/v1/content-packs/subject/" + subjectId);
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

    public Response issueFreemiumPack(String json) {
        return client.post("/api/v1/freemium/packs", json);
    }

    public Response getFreemiumPack(Long packId) {
        return client.get("/api/v1/freemium/packs/" + packId);
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

    public Response deleteClassGrade(Long id) {
        return client.delete("/api/v1/class-grades/" + id);
    }
}
