package com.studyshield.regression.client;

import io.restassured.response.Response;

public class AuthApi {

    private final GatewayClient client;

    public AuthApi(GatewayClient client) {
        this.client = client;
    }

    public Response signUp(String loginId, String password, String name) {
        String json = String.format(
                "{\"loginId\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                loginId, password, name);
        return client.post("/api/auth/signup", json);
    }

    public Response signIn(String loginId, String password) {
        String json = String.format(
                "{\"loginId\": \"%s\", \"password\": \"%s\"}",
                loginId, password);
        return client.post("/api/auth/signin", json);
    }

    public Response validate() {
        return client.post("/api/auth/validate", "{}");
    }

    public Response signOut() {
        return client.post("/api/auth/signout", "{}");
    }
}
