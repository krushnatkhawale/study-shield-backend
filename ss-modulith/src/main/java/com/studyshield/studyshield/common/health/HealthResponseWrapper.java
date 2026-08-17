package com.studyshield.studyshield.common.health;

import org.springframework.boot.actuate.health.Health;

import java.util.Map;

public class HealthResponseWrapper {

    private final String status;
    private final Map<String, Object> details;

    public HealthResponseWrapper(Health health) {
        this.status = health.getStatus().getCode();
        this.details = health.getDetails();
    }

    public String getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
