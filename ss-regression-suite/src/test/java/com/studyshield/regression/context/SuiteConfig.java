package com.studyshield.regression.context;

public class SuiteConfig {

    private final String gatewayBaseUrl;
    private final long readTimeoutMs;
    private final long connectTimeoutMs;
    private final int retryMax;
    private final long retryBackoffMs;
    private final boolean cleanupEnabled;
    private final String prefix;
    private final String authToken;

    public SuiteConfig() {
        this.gatewayBaseUrl = System.getenv("GATEWAY_BASE_URL") != null
                ? System.getenv("GATEWAY_BASE_URL")
                : "http://localhost:8080";
        // Freemium first-issue may seed many rows on Aiven; allow longer default timeout
        this.readTimeoutMs = Long.parseLong(System.getenv().getOrDefault("SUITE_TIMEOUT_MS", "120000"));
        this.connectTimeoutMs = Long.parseLong(System.getenv().getOrDefault("SUITE_CONNECT_TIMEOUT_MS", "15000"));
        this.retryMax = Integer.parseInt(System.getenv().getOrDefault("SUITE_RETRY_MAX", "3"));
        this.retryBackoffMs = Long.parseLong(System.getenv().getOrDefault("SUITE_RETRY_BACKOFF_MS", "2000"));
        this.cleanupEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("SUITE_CLEANUP", "true"));
        this.prefix = System.getenv().getOrDefault("SUITE_PREFIX", "reg_");
        this.authToken = System.getenv().getOrDefault("SUITE_AUTH_TOKEN", "");
    }

    public String getGatewayBaseUrl() { return gatewayBaseUrl; }
    public long getReadTimeoutMs() { return readTimeoutMs; }
    public long getConnectTimeoutMs() { return connectTimeoutMs; }
    public int getRetryMax() { return retryMax; }
    public long getRetryBackoffMs() { return retryBackoffMs; }
    public boolean isCleanupEnabled() { return cleanupEnabled; }
    public String getPrefix() { return prefix; }
    public String getAuthToken() { return authToken; }
}
