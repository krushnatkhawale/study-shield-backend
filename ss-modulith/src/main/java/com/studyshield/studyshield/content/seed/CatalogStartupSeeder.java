package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.service.QuizBundleSeeder;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TD-1: pre-seeds the freemium catalog at startup so {@code POST /api/v1/quiz-bundles}
 * is a pure lookup — no catalog writes in the request path.
 * <p>
 * Seeds one entry per curated band ({@link QuestionBankContent#BANK} key order):
 * Sr KG, Class 1, Exp. Classes without a curated bank are not pre-seeded; requests
 * for them fail fast and the client falls back to bundled assets.
 * <p>
 * Skips cleanly when the academic structure is still empty (fresh environment before
 * {@code POST /api/v1/academic-structure/load/all}) — content cannot be anchored to
 * offerings that do not exist yet.
 */
@Component
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
        List<String> bands = QuestionBankContent.BANK.keySet().stream().toList();
        long start = System.currentTimeMillis();
        for (String className : bands) {
            long classStart = System.currentTimeMillis();
            quizBundleSeeder.ensureCatalogForClass(className, "ALL");
            log.info("[CatalogSeed] Pre-seeded class={} in {}ms", className,
                    System.currentTimeMillis() - classStart);
        }
        log.info("[CatalogSeed] Startup catalog seeding complete for {} classes in {}ms",
                bands.size(), System.currentTimeMillis() - start);
    }
}
