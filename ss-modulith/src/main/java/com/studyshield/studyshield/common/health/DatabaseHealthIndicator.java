package com.studyshield.studyshield.common.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String databaseName = connection.getMetaData().getDatabaseProductName();
                    return Health.up()
                            .withDetail("database", databaseName)
                            .withDetail("connection", "healthy")
                            .build();
                }
            }
            return Health.down().withDetail("database", "Query returned no result").build();

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "DATABASE_UNAVAILABLE")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
