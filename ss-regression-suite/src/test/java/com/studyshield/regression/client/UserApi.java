package com.studyshield.regression.client;

import com.studyshield.regression.context.ScenarioContext;
import io.restassured.response.Response;

public class UserApi {

    private final GatewayClient client;
    private final ScenarioContext context;

    public UserApi(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    public Response createUser(String json) {
        return client.post("/api/v1/users", json);
    }

    public Response getUser(Long id) {
        return client.get("/api/v1/users/" + id);
    }

    public Response getAllUsers() {
        return client.get("/api/v1/users");
    }

    public Response updateUser(Long id, String json) {
        return client.put("/api/v1/users/" + id, json);
    }

    public Response deleteUser(Long id) {
        return client.delete("/api/v1/users/" + id);
    }

    public Response createChild(String json) {
        return client.post("/api/v1/children", json);
    }

    public Response getChild(Long id) {
        return client.get("/api/v1/children/" + id);
    }

    public Response getChildrenByUser(Long userId) {
        return client.get("/api/v1/children/user/" + userId);
    }

    public Response updateChild(Long id, String json) {
        return client.put("/api/v1/children/" + id, json);
    }

    public Response deleteChild(Long id) {
        return client.delete("/api/v1/children/" + id);
    }
}
