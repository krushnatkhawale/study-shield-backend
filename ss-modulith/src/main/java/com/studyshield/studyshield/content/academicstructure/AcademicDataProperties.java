package com.studyshield.studyshield.content.academicstructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the academic-structure data loop (GitHub JSON ↔ database).
 * The ref must be a tag or commit SHA — never a branch — so deploys are reproducible.
 */
@Component
@ConfigurationProperties(prefix = "app.academic-data")
public class AcademicDataProperties {

    /** Where JSON files come from: {@code github} (default) or {@code classpath} (tests). */
    private String source = "github";

    /** Raw file host base, e.g. {@code https://raw.githubusercontent.com}. */
    private String baseUrl = "https://raw.githubusercontent.com";

    /** GitHub {@code owner/repo} that carries {@code academic-structure/*.json}. */
    private String repo = "krushnatkhawale/study-shield-backend";

    /** Pinned tag or commit SHA. Blank = not configured; branches are rejected. */
    private String ref = "";

    /** Directory inside the repo (and classpath) holding the entity JSON files. */
    private String path = "academic-structure";

    /** Hosts the fetcher is allowed to contact (SSRF guard). */
    private List<String> allowedHosts = new ArrayList<>(List.of("raw.githubusercontent.com"));

    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 15000;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getRepo() { return repo; }
    public void setRepo(String repo) { this.repo = repo; }
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<String> getAllowedHosts() { return allowedHosts; }
    public void setAllowedHosts(List<String> allowedHosts) { this.allowedHosts = allowedHosts; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
