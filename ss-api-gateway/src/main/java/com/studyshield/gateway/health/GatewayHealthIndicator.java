package com.studyshield.gateway.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GatewayHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(GatewayHealthIndicator.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    private final String contentServiceUrl;
    private final String userServiceUrl;
    private final String quizAttemptsUrl;
    private final String tvDeviceServiceUrl;

    public GatewayHealthIndicator(
            WebClient.Builder builder,
            @Value("${services.content-service.url}") String contentServiceUrl,
            @Value("${services.user-service.url}") String userServiceUrl,
            @Value("${services.quiz-attempts.url}") String quizAttemptsUrl,
            @Value("${services.tv-device-service.url}") String tvDeviceServiceUrl) {
        this.webClient = builder.build();
        this.contentServiceUrl = contentServiceUrl;
        this.userServiceUrl = userServiceUrl;
        this.quizAttemptsUrl = quizAttemptsUrl;
        this.tvDeviceServiceUrl = tvDeviceServiceUrl;
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
                @SuppressWarnings("unchecked")
                Map<String, Object> result = webClient.get()
                        .uri(check.url + "/actuator/health")
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(TIMEOUT)
                        .block();

                long duration = System.currentTimeMillis() - start;

                if (result != null && "UP".equals(result.get("status"))) {
                    details.put(check.name, Map.of(
                            "status", "UP",
                            "responseTimeMs", duration));
                    healthyCount++;
                } else {
                    details.put(check.name, Map.of(
                            "status", "DOWN",
                            "message", "Service returned status: " +
                                    (result != null ? result.get("status") : "null")));
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
