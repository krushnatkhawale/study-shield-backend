package com.studyshield.studyshield.content.academicstructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches {@code academic-structure/<entity>.json} from raw.githubusercontent.com at
 * the pinned ref. Hard timeouts, host allowlist, entity name in the path only —
 * no free-form URL is ever accepted from the request (no SSRF surface).
 * Registered by {@link AcademicStructureSourceConfig}.
 */
public class GitHubRawAcademicStructureSource implements AcademicStructureFileSource {

    private static final Logger log = LoggerFactory.getLogger(GitHubRawAcademicStructureSource.class);

    private final AcademicDataProperties properties;
    private final HttpClient httpClient;

    public GitHubRawAcademicStructureSource(AcademicDataProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public String describe() {
        return properties.getBaseUrl() + "/" + properties.getRepo() + "/" + properties.getRef()
                + "/" + properties.getPath();
    }

    @Override
    public String describe(AcademicStructureEntity entity) {
        return buildUri(entity).toString();
    }

    @Override
    public String fetchJson(AcademicStructureEntity entity) {
        URI uri = buildUri(entity);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            log.info("[AcademicData] Fetching {}", uri);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code == 404) {
                throw new AcademicStructureConflictException(
                        "Source file not found at " + uri
                                + " — commit academic-structure/" + entity.key()
                                + ".json and pin app.academic-data.ref to that commit.",
                        java.util.Map.of("entity", entity.key(), "sourceUrl", uri.toString(), "httpStatus", 404));
            }
            if (code != 200) {
                throw new IllegalStateException("Fetch failed (HTTP " + code + ") for " + uri);
            }
            return response.body();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not fetch " + uri + ": " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching " + uri, ex);
        }
    }

    private URI buildUri(AcademicStructureEntity entity) {
        if (properties.getRef() == null || properties.getRef().isBlank()) {
            throw new IllegalStateException(
                    "app.academic-data.ref is not configured — pin it to a tag or commit SHA");
        }
        String ref = properties.getRef().trim();
        if ("main".equalsIgnoreCase(ref) || "master".equalsIgnoreCase(ref)) {
            throw new IllegalStateException(
                    "app.academic-data.ref must be a tag or commit SHA, not the branch '" + ref
                            + "' — pin for reproducible deploys");
        }
        String base = properties.getBaseUrl().replaceAll("/+$", "");
        URI uri = URI.create(base + "/" + properties.getRepo() + "/" + ref
                + "/" + properties.getPath() + "/" + entity.key() + ".json");
        String host = uri.getHost();
        boolean allowed = properties.getAllowedHosts().stream().anyMatch(h -> h.equalsIgnoreCase(host));
        if (!allowed) {
            throw new IllegalStateException("Host " + host + " is not in app.academic-data.allowed-hosts");
        }
        return uri;
    }
}
