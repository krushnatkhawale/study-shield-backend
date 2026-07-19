package com.studyshield.gateway.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class GatewayHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(GatewayHealthIndicator.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    private final String contentServiceUrl;
    private final String userServiceUrl;
    private final String quizAttemptsUrl;
    private final String tvDeviceServiceUrl;

    public GatewayHealthIndicator(WebClient.Builder builder) {
        this.webClient = builder.build();
        this.contentServiceUrl = System.getenv("CONTENT_SERVICE_URL") != null
                ? System.getenv("CONTENT_SERVICE_URL") : "http://localhost:8081";
        this.userServiceUrl = System.getenv("USER_SERVICE_URL") != null
                ? System.getenv("USER_SERVICE_URL") : "http://localhost:8082";
        this.quizAttemptsUrl = System.getenv("QUIZ_ATTEMPTS_URL") != null
                ? System.getenv("QUIZ_ATTEMPTS_URL") : "http://localhost:8083";
        this.tvDeviceServiceUrl = System.getenv("TV_DEVICE_SERVICE_URL") != null
                ? System.getenv("TV_DEVICE_SERVICE_URL") : "http://localhost:8084";
    }

    @Override
    public Health health() {
        List<ServiceCheck> checks = List.of(
                new ServiceCheck("content-service", contentServiceUrl),
                new ServiceCheck("user-service", userServiceUrl),
                new ServiceCheck("quiz-attempts", quizAttemptsUrl),
                new ServiceCheck("tv-device-service", tvDeviceServiceUrl)
        );

        Map<String, Object> details = new LinkedHashMap<>();
        int healthyCount = 0;

        for (ServiceCheck check : checks) {
            try {
                long start = System.currentTimeMillis();
                HealthComponent result = webClient.get()
                        .uri(check.url + "/actuator/health")
                        .retrieve()
                        .bodyToMono(HealthComponent.class)
                        .timeout(TIMEOUT)
                        .block();

                long duration = System.currentTimeMillis() - start;

                if (result != null && Status.UP.equals(result.getStatus())) {
                    details.put(check.name, Map.of(
                            "status", "UP",
                            "responseTimeMs", duration));
                    healthyCount++;
                } else {
                    details.put(check.name, Map.of(
                            "status", "DOWN",
                            "message", "Service returned status: " +
                                    (result != null ? result.getStatus() : "null")));
                }
            } catch (Exception e) {
                log.warn("Health check failed for {}: {}", check.name, e.getMessage());
                details.put(check.name, Map.of(
                        "status", "DOWN",
                        "error", e.getClass().getSimpleName(),
                        "message", e.getMessage()));
            }
        }

        details.put("healthyServices", healthyCount + "/" + checks.size());

        if (healthyCount == checks.size()) {
            return Health.up().withDetails(details).build();
        } else if (healthyCount > 0) {
            return Health.status("DEGRADED").withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }

    private record ServiceCheck(String name, String url) {}
}
