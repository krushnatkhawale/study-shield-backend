package com.studyshield.regression.context;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import java.util.Map;

public class ScenarioContext {

    private int lastStatusCode;
    private String lastResponseBody;
    private Headers lastResponseHeaders;
    private long lastResponseTimeMs;
    private String lastRequestMethod;
    private String lastRequestUrl;
    private String lastRequestBody;
    private Response lastResponse;

    private final Map<String, Long> createdIds = new java.util.concurrent.ConcurrentHashMap<>();
    private final String suitePrefix;

    private Long currentUserId;
    private Long currentChildId;
    private Long currentBoardId;
    private Long currentClassGradeId;
    private Long currentSubjectId;
    private Long currentContentPackId;
    private Long currentQuizId;
    private Long currentFreemiumPackId;
    private Long currentAttemptId;
    private Long currentWifiNetworkId;
    private Long currentConnectedTvId;
    private Long currentTvUserId;
    private Long capturedId;

    public ScenarioContext() {
        this.suitePrefix = "reg_" + System.currentTimeMillis() + "_";
    }

    public String getSuitePrefix() { return suitePrefix; }
    public String uniqueName(String base) { return suitePrefix + base; }

    public int getLastStatusCode() { return lastStatusCode; }
    public void setLastStatusCode(int lastStatusCode) { this.lastStatusCode = lastStatusCode; }
    public String getLastResponseBody() { return lastResponseBody; }
    public void setLastResponseBody(String lastResponseBody) { this.lastResponseBody = lastResponseBody; }
    public Headers getLastResponseHeaders() { return lastResponseHeaders; }
    public void setLastResponseHeaders(Headers lastResponseHeaders) { this.lastResponseHeaders = lastResponseHeaders; }
    public long getLastResponseTimeMs() { return lastResponseTimeMs; }
    public void setLastResponseTimeMs(long lastResponseTimeMs) { this.lastResponseTimeMs = lastResponseTimeMs; }
    public String getLastRequestMethod() { return lastRequestMethod; }
    public void setLastRequestMethod(String lastRequestMethod) { this.lastRequestMethod = lastRequestMethod; }
    public String getLastRequestUrl() { return lastRequestUrl; }
    public void setLastRequestUrl(String lastRequestUrl) { this.lastRequestUrl = lastRequestUrl; }
    public String getLastRequestBody() { return lastRequestBody; }
    public void setLastRequestBody(String lastRequestBody) { this.lastRequestBody = lastRequestBody; }
    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public Map<String, Long> getCreatedIds() { return createdIds; }
    public void registerId(String key, Long id) { createdIds.put(key, id); }

    public Long getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(Long currentUserId) { this.currentUserId = currentUserId; }
    public Long getCurrentChildId() { return currentChildId; }
    public void setCurrentChildId(Long currentChildId) { this.currentChildId = currentChildId; }
    public Long getCurrentBoardId() { return currentBoardId; }
    public void setCurrentBoardId(Long id) { this.currentBoardId = id; }
    public Long getCurrentClassGradeId() { return currentClassGradeId; }
    public void setCurrentClassGradeId(Long id) { this.currentClassGradeId = id; }
    public Long getCurrentSubjectId() { return currentSubjectId; }
    public void setCurrentSubjectId(Long id) { this.currentSubjectId = id; }
    public Long getCurrentContentPackId() { return currentContentPackId; }
    public void setCurrentContentPackId(Long id) { this.currentContentPackId = id; }
    public Long getCurrentQuizId() { return currentQuizId; }
    public void setCurrentQuizId(Long id) { this.currentQuizId = id; }
    public Long getCurrentFreemiumPackId() { return currentFreemiumPackId; }
    public void setCurrentFreemiumPackId(Long id) { this.currentFreemiumPackId = id; }
    public Long getCurrentAttemptId() { return currentAttemptId; }
    public void setCurrentAttemptId(Long id) { this.currentAttemptId = id; }
    public Long getCurrentWifiNetworkId() { return currentWifiNetworkId; }
    public void setCurrentWifiNetworkId(Long id) { this.currentWifiNetworkId = id; }
    public Long getCurrentConnectedTvId() { return currentConnectedTvId; }
    public void setCurrentConnectedTvId(Long id) { this.currentConnectedTvId = id; }
    public Long getCurrentTvUserId() { return currentTvUserId; }
    public void setCurrentTvUserId(Long id) { this.currentTvUserId = id; }
    public Long getCapturedId() { return capturedId; }
    public void setCapturedId(Long id) { this.capturedId = id; }

    public String getGatewayUrl() {
        return System.getenv("GATEWAY_BASE_URL") != null
                ? System.getenv("GATEWAY_BASE_URL")
                : "http://localhost:8080";
    }

    public void reset() {
        lastStatusCode = 0;
        lastResponseBody = null;
        lastResponseHeaders = null;
        lastResponseTimeMs = 0;
        lastRequestMethod = null;
        lastRequestUrl = null;
        lastRequestBody = null;
        lastResponse = null;
        createdIds.clear();
        currentUserId = null;
        currentChildId = null;
        currentBoardId = null;
        currentClassGradeId = null;
        currentSubjectId = null;
        currentContentPackId = null;
        currentQuizId = null;
        currentFreemiumPackId = null;
        currentAttemptId = null;
        currentWifiNetworkId = null;
        currentConnectedTvId = null;
        currentTvUserId = null;
        capturedId = null;
    }
}
