package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.AuthApi;
import com.studyshield.regression.client.UserApi;
import com.studyshield.regression.context.AuthContext;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserSteps {

    private static final String TEST_PASSWORD = "Test1234!";

    private final UserApi userApi;
    private final AuthApi authApi;
    private final ScenarioContext context;
    private final AuthContext authContext;
    private final IdRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserSteps(UserApi userApi, AuthApi authApi, ScenarioContext context, AuthContext authContext, IdRegistry registry) {
        this.userApi = userApi;
        this.authApi = authApi;
        this.context = context;
        this.authContext = authContext;
        this.registry = registry;
    }

    @Given("I am authenticated as a parent user")
    public void iAmAuthenticatedAsAParentUser() {
        String email = context.uniqueName("auth_parent@test.com");
        Response signupResponse = authApi.signUp(email, TEST_PASSWORD, context.uniqueName("Auth Parent"));
        assertThat(signupResponse.getStatusCode())
                .as("Signup failed: %s", signupResponse.getBody().asString())
                .isEqualTo(200);

        String token = signupResponse.jsonPath().getString("sessionId");
        assertThat(token).as("JWT token must not be null").isNotNull();
        authContext.setJwtToken(token);

        Long accountId = signupResponse.jsonPath().getLong("accountId");
        context.setCurrentUserId(accountId);
        registry.register("auth_user", accountId);

        context.setLastResponse(signupResponse);
        context.setLastStatusCode(signupResponse.getStatusCode());
        context.setLastResponseBody(signupResponse.getBody().asString());
    }

    @Given("a parent user exists")
    public void aParentUserExists() throws Exception {
        iAmAuthenticatedAsAParentUser();
        String email = context.uniqueName("parent@test.com");
        String json = mapper.writeValueAsString(Map.of(
                "email", email,
                "name", context.uniqueName("Test Parent"),
                "phone", "9876543210",
                "role", "PARENT",
                "active", true
        ));
        Response response = userApi.createUser(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentUserId(id);
        registry.register("user", id);
    }

    @When("I create a parent user with name {string}")
    public void iCreateParentUserWithName(String name) throws Exception {
        iAmAuthenticatedAsAParentUser();
        String email = context.uniqueName(name.toLowerCase().replace(" ", "_") + "@test.com");
        String json = mapper.writeValueAsString(Map.of(
                "email", email,
                "name", context.uniqueName(name),
                "phone", "9876543210",
                "role", "PARENT",
                "active", true
        ));
        Response response = userApi.createUser(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentUserId(id);
            registry.register("user", id);
        }
    }

    @When("I get user by id")
    public void iGetUserById() {
        Response response = userApi.getUser(context.getCurrentUserId());
        updateContext(response);
    }

    @When("I get all users")
    public void iGetAllUsers() {
        Response response = userApi.getAllUsers();
        updateContext(response);
    }

    @When("I delete the current user")
    public void iDeleteCurrentUser() {
        Response response = userApi.deleteUser(context.getCurrentUserId());
        updateContext(response);
    }

    @Given("a child profile {string} exists under current user")
    public void aChildProfileExistsUnderCurrentUser(String childName) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(childName),
                "age", 8,
                "userId", context.getCurrentUserId(),
                "boardId", 1,
                "classGradeId", 1,
                "active", true
        ));
        Response response = userApi.createChild(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentChildId(id);
        registry.register("child", id);
    }

    @When("I create a child profile {string}")
    public void iCreateChildProfile(String childName) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "name", context.uniqueName(childName),
                "age", 8,
                "userId", context.getCurrentUserId(),
                "active", true
        ));
        Response response = userApi.createChild(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentChildId(id);
            registry.register("child", id);
        }
    }

    @When("I get children for current user")
    public void iGetChildrenForCurrentUser() {
        Response response = userApi.getChildrenByUser(context.getCurrentUserId());
        updateContext(response);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
