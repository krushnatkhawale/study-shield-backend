package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.config.CatalogSeedConfig;
import com.studyshield.studyshield.content.service.QuizBundleSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TD-1 / FEAT-1793475593350: catalog seeding must never run inside a request transaction.
 * <p>
 * The catalog is pre-seeded at startup ({@link CatalogStartupSeeder}). When a request still
 * misses (a class that appeared after boot), the request fails fast and this component
 * queues the seed on the dedicated catalog-seed thread. The next request is served by
 * read-only lookups. Concurrent misses for the same class share one queued seed.
 */
@Component
public class CatalogMissSeeder {

    private static final Logger log = LoggerFactory.getLogger(CatalogMissSeeder.class);

    private final QuizBundleSeeder quizBundleSeeder;
    private final TaskExecutor taskExecutor;
    private final boolean asyncOnMiss;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public CatalogMissSeeder(
            QuizBundleSeeder quizBundleSeeder,
            @Qualifier(CatalogSeedConfig.CATALOG_SEED_EXECUTOR) TaskExecutor taskExecutor,
            @Value("${app.catalog-seeding.async-on-miss:true}") boolean asyncOnMiss
    ) {
        this.quizBundleSeeder = quizBundleSeeder;
        this.taskExecutor = taskExecutor;
        this.asyncOnMiss = asyncOnMiss;
    }

    /**
     * Queue a background seed for a missed class. Returns {@code true} when a task was
     * queued; {@code false} when seeding is disabled or the same class is already queued.
     * Never blocks the caller and never throws.
     */
    public boolean seedAsync(String className, String boardCode) {
        if (!asyncOnMiss || className == null || className.isBlank()) {
            return false;
        }
        String key = boardKey(boardCode) + "|" + className.trim().toLowerCase(Locale.ROOT);
        if (!inFlight.add(key)) {
            return false;
        }
        try {
            taskExecutor.execute(() -> runSeed(className, boardCode, key));
            return true;
        } catch (RuntimeException e) {
            inFlight.remove(key);
            log.warn("[CatalogSeed] Could not queue async seed for class={} board={}: {}",
                    className, boardKey(boardCode), e.getMessage());
            return false;
        }
    }

    private void runSeed(String className, String boardCode, String key) {
        long start = System.currentTimeMillis();
        try {
            quizBundleSeeder.ensureCatalogForClass(className, boardCode);
            log.info("[CatalogSeed] Async on-miss seed for class={} board={} finished in {}ms",
                    className, boardKey(boardCode), System.currentTimeMillis() - start);
        } catch (RuntimeException e) {
            log.warn("[CatalogSeed] Async on-miss seed for class={} board={} failed: {}",
                    className, boardKey(boardCode), e.getMessage(), e);
        } finally {
            inFlight.remove(key);
        }
    }

    private static String boardKey(String boardCode) {
        return boardCode == null || boardCode.isBlank()
                ? "ALL"
                : boardCode.trim().toUpperCase(Locale.ROOT);
    }
}
