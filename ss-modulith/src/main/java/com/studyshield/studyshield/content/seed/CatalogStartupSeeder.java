package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.service.QuizBundleSeeder;
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
 */
@Component
public class CatalogStartupSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogStartupSeeder.class);

    private final QuizBundleSeeder quizBundleSeeder;

    public CatalogStartupSeeder(QuizBundleSeeder quizBundleSeeder) {
        this.quizBundleSeeder = quizBundleSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
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
