package com.studyshield.regression.steps;

import com.studyshield.regression.client.GatewayClient;
import com.studyshield.regression.context.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class GatewaySteps {

    private final GatewayClient client;
    private final ScenarioContext context;

    public GatewaySteps(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    @When("I GET {string}")
    public void iGET(String path) {
        Response response = client.get(path);
        updateContext("GET", path, null, response);
    }

    @When("I POST {string} with body:")
    public void iPOSTWithBody(String path, String body) {
        Response response = client.post(path, body);
        updateContext("POST", path, body, response);
    }

    @When("I PUT {string} with body:")
    public void iPUTWithBody(String path, String body) {
        Response response = client.put(path, body);
        updateContext("PUT", path, body, response);
    }

    @When("I DELETE {string}")
    public void iDELETE(String path) {
        Response response = client.delete(path);
        updateContext("DELETE", path, null, response);
    }

    private void updateContext(String method, String path, String body, Response response) {
        context.setLastRequestMethod(method);
        context.setLastRequestUrl(path);
        context.setLastRequestBody(body);
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseHeaders(response.getHeaders());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
