package com.studyshield.regression.client;

import com.studyshield.regression.context.AuthContext;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.context.SuiteConfig;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class GatewayClient {

    private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

    private final SuiteConfig config;
    private final AuthContext authContext;

    public GatewayClient(SuiteConfig config, AuthContext authContext) {
        this.config = config;
        this.authContext = authContext;
        RestAssured.baseURI = config.getGatewayBaseUrl();
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", (int) config.getConnectTimeoutMs())
                        .setParam("http.socket.timeout", (int) config.getReadTimeoutMs()));
    }

    public Response get(String path) {
        return executeWithRetry("GET", path, null);
    }

    public Response post(String path, String jsonBody) {
        return executeWithRetry("POST", path, jsonBody);
    }

    public Response put(String path, String jsonBody) {
        return executeWithRetry("PUT", path, jsonBody);
    }

    public Response put(String path) {
        return executeWithRetry("PUT", path, null);
    }

    public Response delete(String path) {
        return executeWithRetry("DELETE", path, null);
    }

    private Response executeWithRetry(String method, String path, String body) {
        Response lastResponse = null;
        for (int attempt = 0; attempt <= config.getRetryMax(); attempt++) {
            try {
                RequestSpecification req = RestAssured.given()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json");

                if (authContext.hasToken()) {
                    req.header("Authorization", "Bearer " + authContext.getJwtToken());
                }

                if (body != null) {
                    req.body(body);
                }

                long start = System.currentTimeMillis();
                lastResponse = switch (method) {
                    case "GET" -> req.get(path);
                    case "POST" -> req.post(path);
                    case "PUT" -> req.put(path);
                    case "DELETE" -> req.delete(path);
                    default -> throw new IllegalArgumentException("Unsupported method: " + method);
                };
                long elapsed = System.currentTimeMillis() - start;

                log.debug("[Gateway] {} {} -> {} ({}ms)", method, path, lastResponse.getStatusCode(), elapsed);

                int status = lastResponse.getStatusCode();
                if (status != 502 && status != 503 && status != 504) {
                    return lastResponse;
                }

                log.warn("[Gateway] {} {} returned {} (attempt {}/{}), retrying...",
                        method, path, status, attempt + 1, config.getRetryMax() + 1);
                Thread.sleep(config.getRetryBackoffMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during retry", e);
            } catch (Exception e) {
                log.warn("[Gateway] {} {} failed (attempt {}/{}): {}",
                        method, path, attempt + 1, config.getRetryMax() + 1, e.getMessage());
                if (attempt == config.getRetryMax()) {
                    throw new RuntimeException("Gateway request failed after " + (config.getRetryMax() + 1) + " attempts", e);
                }
                try {
                    Thread.sleep(config.getRetryBackoffMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
            }
        }
        return lastResponse;
    }
}
