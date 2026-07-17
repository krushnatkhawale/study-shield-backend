package com.studyshield.regression.fixtures;

import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class UserFixtures {

    private final ScenarioContext context;
    private final IdRegistry registry;
    private final PayloadFactory payloads;

    public UserFixtures(ScenarioContext context, IdRegistry registry, PayloadFactory payloads) {
        this.context = context;
        this.registry = registry;
        this.payloads = payloads;
    }

    public Long createUser(String name, String role) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.user(name, role))
                .post("/api/users")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentUserId(id);
        registry.register("user", id);
        return id;
    }

    public Long createParent(String name) {
        return createUser(name, "PARENT");
    }

    public Long createChild(String name, Long userId) {
        Response response = io.restassured.RestAssured.given()
                .baseUri(context.getGatewayUrl())
                .contentType("application/json")
                .body(payloads.child(name, userId))
                .post("/api/users/children")
                .then().statusCode(201).extract().response();
        Long id = response.jsonPath().getLong("id");
        context.setCurrentChildId(id);
        registry.register("child", id);
        return id;
    }

    public void createParentWithChild(String parentName, String childName) {
        Long userId = createParent(parentName);
        createChild(childName, userId);
    }
}
