package com.studyshield.content.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    private static final String HEALTH_CHECK_QUERY = "SELECT 1";

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            return checkDatabaseHealth();
        } catch (Exception e) {
            log.warn("Database health check failed: {}", e.getMessage());
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("message", e.getMessage());
            return Health.down().withDetails(details).build();
        }
    }

    private Health checkDatabaseHealth() throws SQLException {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(HEALTH_CHECK_QUERY);
             ResultSet rs = stmt.executeQuery()) {
            long duration = System.currentTimeMillis() - startTime;
            if (rs.next() && rs.getInt(1) == 1) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("database", connection.getMetaData().getDatabaseProductName());
                details.put("responseTimeMs", duration);
                return Health.up().withDetails(details).build();
            } else {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("error", "UnexpectedResult");
                details.put("message", "Health check query returned unexpected result");
                return Health.down().withDetails(details).build();
            }
        }
    }
}
