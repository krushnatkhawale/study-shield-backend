package com.studyshield.regression.support;

import com.studyshield.regression.client.GatewayClient;
import com.studyshield.regression.context.SuiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final GatewayClient client;
    private final SuiteConfig config;
    private final IdRegistry registry;

    public CleanupService(GatewayClient client, SuiteConfig config, IdRegistry registry) {
        this.client = client;
        this.config = config;
        this.registry = registry;
    }

    public void cleanupAll() {
        if (!config.isCleanupEnabled()) {
            log.info("[Cleanup] SUITE_CLEANUP=false, skipping");
            return;
        }

        log.info("[Cleanup] Starting cleanup of {} entity types", registry.getAllRegistered().size());

        deleteInOrder("attempt-answer", "/api/v1/quiz-attempts/answers/");
        deleteInOrder("quiz-attempt", "/api/v1/quiz-attempts/");
        deleteInOrder("question", "/api/v1/questions/");
        deleteInOrder("quiz", "/api/v1/quizzes/");
        deleteInOrder("content-pack", "/api/v1/content-packs/");
        deleteInOrder("subject", "/api/v1/subjects/");
        deleteInOrder("class-grade", "/api/v1/class-grades/");
        deleteInOrder("board", "/api/v1/boards/");
        deleteInOrder("connected-tv", "/api/v1/connected-tvs/");
        deleteInOrder("wifi-network", "/api/v1/wifi-networks/");
        deleteInOrder("child", "/api/v1/children/");
        deleteInOrder("user", "/api/v1/users/");

        registry.clear();
        log.info("[Cleanup] Complete");
    }

    private void deleteInOrder(String entityType, String pathPrefix) {
        var ids = registry.getAll(entityType);
        for (Long id : ids) {
            try {
                client.delete(pathPrefix + id);
                log.debug("[Cleanup] Deleted {} id={}", entityType, id);
            } catch (Exception e) {
                log.warn("[Cleanup] Failed to delete {} id={}: {}", entityType, id, e.getMessage());
            }
        }
    }
}
