package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.service.QuizBundleSeeder;
import com.studyshield.studyshield.content.seed.QuestionBankContent;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * TD-1: pre-seeds the freemium catalog at startup so {@code POST /api/v1/quiz-bundles}
 * is a pure lookup — no catalog writes in the request path.
 * <p>
 * Seeds one entry per curated band ({@link QuestionBankContent#BANK} key order):
 * PreNursery through Class 10. Classes without a curated bank are not pre-seeded;
 * requests for them fail fast and the client falls back to bundled assets. A curated
 * class that still misses at runtime is seeded in the background by
 * {@link CatalogMissSeeder} — never in the request transaction.
 * <p>
 * Skips cleanly when the academic structure is still empty (fresh environment before
 * {@code POST /api/v1/academic-structure/load/all}) — content cannot be anchored to
 * offerings that do not exist yet.
 */
@Component
@Order(100)
public class CatalogStartupSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogStartupSeeder.class);

    private final QuizBundleSeeder quizBundleSeeder;
    private final ClassLevelRepository classLevelRepository;

    @Value("${app.catalog-seeding.enabled:true}")
    private boolean seedingEnabled;

    public CatalogStartupSeeder(QuizBundleSeeder quizBundleSeeder,
                                  ClassLevelRepository classLevelRepository) {
        this.quizBundleSeeder = quizBundleSeeder;
        this.classLevelRepository = classLevelRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedingEnabled) {
            log.info("[CatalogSeed] Disabled via app.catalog-seeding.enabled=false — skipping");
            return;
        }
        if (classLevelRepository.count() == 0) {
            log.warn("[CatalogSeed] Academic structure is empty — skipping catalog seeding."
                    + " Run POST /api/v1/academic-structure/load/all once, then restart"
                    + " (or POST /api/v1/quiz-bundles/rebuild-catalog).");
            return;
        }
        
        // Ensure we have a clean list of classes to seed, including all possible class names that could be requested
        Set<String> bands = QuestionBankContent.BANK.keySet();
        List<String> classNames = bands.stream().toList();
        
        long start = System.currentTimeMillis();
        int seeded = 0;
        int totalClasses = 0;
        
        // Pre-seed curated classes in batches to improve performance
        for (String className : classNames) {
            long classStart = System.currentTimeMillis();
            totalClasses++;
            try {
                quizBundleSeeder.ensureCatalogForClass(className, "ALL");
                seeded++;
                log.info("[CatalogSeed] Pre-seeded class={} in {}ms", className,
                        System.currentTimeMillis() - classStart);
            } catch (RuntimeException e) {
                // One unresolvable band must never stop the application from booting;
                // requests for it fail fast and fall back to bundled content.
                log.warn("[CatalogSeed] Skipped class={} in {}ms: {}", className,
                        System.currentTimeMillis() - classStart, e.getMessage());
            }
        }
        
        // Also pre-seed all classes from all existing class levels to cover edge cases
        try {
            List<String> levelClasses = classLevelRepository.findAllByOrderByOrdinalAsc().stream()
                    .map(level -> level.getCanonicalName())
                    .filter(name -> name != null && !name.isBlank())
                    .toList();
            
            for (String className : levelClasses) {
                totalClasses++;
                long classStart = System.currentTimeMillis();
                if (!bands.contains(className)) { // Only seed non-curated classes if they aren't already seeded
                    try {
                        quizBundleSeeder.ensureCatalogForClass(className, "ALL");
                        seeded++;
                        log.info("[CatalogSeed] Pre-seeded level class={} in {}ms", className,
                                System.currentTimeMillis() - classStart);
                    } catch (Exception e) {
                        log.warn("[CatalogSeed] Skipped level class={} in {}ms: {}", className,
                                System.currentTimeMillis() - classStart, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error while pre-seeding from class levels: {}", e.getMessage());
        }
        
        log.info("[CatalogSeed] Startup catalog seeding complete: {}/{} classes in {}ms",
                seeded, totalClasses, System.currentTimeMillis() - start);
    }
}
