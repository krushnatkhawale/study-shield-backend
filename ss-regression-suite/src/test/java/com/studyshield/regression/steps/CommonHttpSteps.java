package com.studyshield.regression.steps;

import com.studyshield.regression.client.GatewayClient;
import com.studyshield.regression.context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonHttpSteps {

    private final ScenarioContext context;
    private final GatewayClient client;

    public CommonHttpSteps(ScenarioContext context, GatewayClient client) {
        this.context = context;
        this.client = client;
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(context.getLastStatusCode())
                .as("Expected status %d but got %d. Response: %s",
                        expectedStatus, context.getLastStatusCode(),
                        context.getLastResponseBody() != null
                                ? context.getLastResponseBody().substring(0, Math.min(500, context.getLastResponseBody().length()))
                                : "null")
                .isEqualTo(expectedStatus);
    }

    @Then("the response status should be {int} or {int}")
    public void theResponseStatusShouldBeOneOf(int status1, int status2) {
        assertThat(context.getLastStatusCode())
                .as("Expected status %d or %d but got %d. Response: %s",
                        status1, status2, context.getLastStatusCode(),
                        context.getLastResponseBody() != null
                                ? context.getLastResponseBody().substring(0, Math.min(500, context.getLastResponseBody().length()))
                                : "null")
                .isIn(status1, status2);
    }

    @Then("the response body should be a JSON array")
    public void theResponseBodyShouldBeJsonArray() {
        assertThat(context.getLastResponseBody()).isNotNull();
        assertThat(context.getLastResponseBody().trim()).startsWith("[");
    }

    @Then("the response body should be a JSON object")
    public void theResponseBodyShouldBeJsonObject() {
        assertThat(context.getLastResponseBody()).isNotNull();
        assertThat(context.getLastResponseBody().trim()).startsWith("{");
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String expected) {
        assertThat(context.getLastResponseBody()).contains(expected);
    }

    @Then("the response JSON path {string} should equal {string}")
    public void theResponseJsonPathShouldEqual(String path, String expected) {
        Response response = getLastResponse();
        String actual = response.jsonPath().getString(path);
        assertThat(actual).isEqualTo(expected);
    }

    @Then("the response JSON path {string} should be present")
    public void theResponseJsonPathShouldBePresent(String path) {
        Response response = getLastResponse();
        Object value = response.jsonPath().get(path);
        assertThat((Object) value).isNotNull();
    }

    @Then("the response JSON path {string} should be a list with size {int}")
    public void theResponseJsonPathShouldBeListWithSize(String path, int expectedSize) {
        Response response = getLastResponse();
        java.util.List<?> list = response.jsonPath().getList(path);
        assertThat(list).hasSize(expectedSize);
    }

    @Then("the response should have an id field")
    public void theResponseShouldHaveIdField() {
        Response response = getLastResponse();
        Object id = response.jsonPath().get("id");
        assertThat((Object) id).isNotNull();
    }

    @When("I capture the response id")
    public void iCaptureTheResponseId() {
        Response response = getLastResponse();
        Long id = response.jsonPath().getLong("id");
        assertThat(id).as("Response must contain an 'id' field").isNotNull();
        context.setCapturedId(id);
    }

    @When("I DELETE the captured parent")
    public void iDeleteTheCapturedParent() {
        Long id = context.getCapturedId();
        assertThat(id).as("No captured id to delete").isNotNull();
        Response response = client.delete("/api/parents/" + id);
        updateContext(response);
    }

    @When("I DELETE the captured student")
    public void iDeleteTheCapturedStudent() {
        Long id = context.getCapturedId();
        assertThat(id).as("No captured id to delete").isNotNull();
        Response response = client.delete("/api/students/" + id);
        updateContext(response);
    }

    @When("I update the captured student with body:")
    public void iUpdateTheCapturedStudentWithBody(String body) {
        Long id = context.getCapturedId();
        assertThat(id).as("No captured id to update").isNotNull();
        Response response = client.put("/api/students/" + id, body);
        updateContext(response);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }

    private Response getLastResponse() {
        Response response = context.getLastResponse();
        assertThat(response)
                .as("No response captured in context. A When/Given step must execute before this Then step.")
                .isNotNull();
        return response;
    }
}
