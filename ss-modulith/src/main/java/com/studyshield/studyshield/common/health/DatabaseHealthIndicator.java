package com.studyshield.studyshield.common.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    @Override
    public Health health() {
        try (Connection connection = DriverManager.getConnection(
                getEnvOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/studyshield"),
                getEnvOrDefault("DB_USERNAME", "postgres"),
                getEnvOrDefault("DB_PASSWORD", "postgres"))) {

            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("connection", "healthy")
                            .build();
                }
            }
            return Health.down().withDetail("database", "Query returned no result").build();

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
