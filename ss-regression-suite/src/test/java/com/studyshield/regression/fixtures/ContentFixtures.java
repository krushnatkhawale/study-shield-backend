package com.studyshield.regression.fixtures;

import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentFixtures {

    private final ScenarioContext context;
    private final IdRegistry registry;
    private final PayloadFactory payloads;

    public ContentFixtures(ScenarioContext context, IdRegistry registry, PayloadFactory payloads) {
        this.context = context;
        this.registry = registry;
        this.payloads = payloads;
    }

    public Long createBoard(String name) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.board(name))
                .post("/api/content/boards")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentBoardId(id);
        registry.register("board", id);
        return id;
    }

    public Long createClassGrade(int gradeNumber, Long boardId) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.classGrade(gradeNumber, boardId))
                .post("/api/content/class-grades")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentClassGradeId(id);
        registry.register("class-grade", id);
        return id;
    }

    public Long createSubject(String name, Long classGradeId) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.subject(name, classGradeId))
                .post("/api/content/subjects")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentSubjectId(id);
        registry.register("subject", id);
        return id;
    }

    public Long createContentPack(String name, Long subjectId) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.contentPack(name, subjectId))
                .post("/api/content/content-packs")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentContentPackId(id);
        registry.register("content-pack", id);
        return id;
    }

    public Long createQuiz(String title, Long contentPackId, String quizType) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.quiz(title, contentPackId, quizType))
                .post("/api/content/quizzes")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentQuizId(id);
        registry.register("quiz", id);
        return id;
    }

    public void createQuestion(String text, Long quizId, int orderIndex) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.question(text, quizId, orderIndex))
                .post("/api/content/questions")
                .then().statusCode(201).extract().response();
        registry.register("question", response.jsonPath().getLong("id"));
    }

    public void createFullContentHierarchy() {
        Long boardId = createBoard("Regression Board");
        Long classGradeId = createClassGrade(10, boardId);
        Long subjectId = createSubject("Physics", classGradeId);
        Long contentPackId = createContentPack("Electromagnetism", subjectId);
        createQuiz("Magnetism Quiz", contentPackId, "STANDARD");
    }
}
