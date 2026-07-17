package com.studyshield.regression.hooks;

import com.studyshield.regression.context.ScenarioContext;
import io.cucumber.java.After;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportingHooks {

    private static final Logger log = LoggerFactory.getLogger(ReportingHooks.class);

    private final ScenarioContext context;

    public ReportingHooks(ScenarioContext context) {
        this.context = context;
    }

    @After(order = 50)
    public void attachRequestResponseOnFailure(io.cucumber.java.Scenario scenario) {
        if (scenario.isFailed()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Method: ").append(context.getLastRequestMethod()).append("\n");
            sb.append("URL: ").append(context.getLastRequestUrl()).append("\n");
            sb.append("Status: ").append(context.getLastStatusCode()).append("\n");
            sb.append("Time: ").append(context.getLastResponseTimeMs()).append("ms\n\n");

            if (context.getLastRequestBody() != null) {
                String body = context.getLastRequestBody();
                if (body.length() > 2000) body = body.substring(0, 2000) + "...(truncated)";
                sb.append("Request Body:\n").append(body).append("\n\n");
            }

            if (context.getLastResponseBody() != null) {
                String body = context.getLastResponseBody();
                if (body.length() > 5000) body = body.substring(0, 5000) + "...(truncated)";
                sb.append("Response Body:\n").append(body);
            }

            scenario.attach(sb.toString(), "text/plain", "Request/Response Details");
            log.error("[Reporting] Attached request/response for failed scenario: {}", scenario.getName());
        }
    }
}
