package com.studyshield.regression.hooks;

import com.studyshield.regression.client.GatewayClient;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.context.SuiteConfig;
import com.studyshield.regression.support.CleanupService;
import com.studyshield.regression.support.IdRegistry;
import com.studyshield.regression.support.ServiceLauncher;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalHooks {

    private static final Logger log = LoggerFactory.getLogger(GlobalHooks.class);

    private static final ServiceLauncher serviceLauncher = new ServiceLauncher();

    private final GatewayClient client;
    private final ScenarioContext context;
    private final SuiteConfig config;
    private final IdRegistry registry;
    private final CleanupService cleanupService;

    public GlobalHooks(GatewayClient client, ScenarioContext context, SuiteConfig config,
                       IdRegistry registry, CleanupService cleanupService) {
        this.client = client;
        this.context = context;
        this.config = config;
        this.registry = registry;
        this.cleanupService = cleanupService;
    }

    @Before
    public void beforeScenario() {
        String databaseUrl = System.getenv("DATABASE_URL");
        log.info("[Hooks] DATABASE_URL='{}'", databaseUrl);
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            log.warn("[Hooks] DATABASE_URL not set - skipping regression tests");
            return;
        }

        if (!serviceLauncher.isStarted()) {
            log.info("[Hooks] Starting all services...");
            serviceLauncher.startAllServices();
        }
        context.reset();
        log.info("[Hooks] Scenario started, prefix={}", context.getSuitePrefix());
    }

    @After(order = 100)
    public void afterScenario(io.cucumber.java.Scenario scenario) {
        log.info("[Hooks] Scenario {} finished: {}", scenario.getName(), scenario.getStatus());
        cleanupService.cleanupAll();
    }

    @Given("the regression suite targets the API gateway")
    public void theRegressionSuiteTargetsTheApiGateway() {
        log.info("[Hooks] Targeting gateway: {}", config.getGatewayBaseUrl());
    }

    @Given("the gateway is ready within the configured timeout")
    public void theGatewayIsReadyWithinConfiguredTimeout() {
        Response response = client.get("/actuator/health");
        assertThat(response.getStatusCode())
                .as("Gateway health check failed - is the gateway running at %s?", config.getGatewayBaseUrl())
                .isEqualTo(200);
    }
}
