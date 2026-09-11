package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.dto.FreemiumRebuildResponse;
import com.studyshield.studyshield.content.service.QuizBundleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * One-shot: set {@code app.freemium-rebuild-on-startup=true} (or env
 * {@code FREEMIUM_REBUILD_ON_STARTUP=true}) to drop issued quiz bundles and
 * seed every class/subject that has questions. Turn it off again after one boot.
 */
@Component
@Order(200)
public class FreemiumCatalogRebuildRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FreemiumCatalogRebuildRunner.class);

    private final QuizBundleService quizBundleService;

    @Value("${app.freemium-rebuild-on-startup:false}")
    private boolean rebuildOnStartup;

    public FreemiumCatalogRebuildRunner(QuizBundleService quizBundleService) {
        this.quizBundleService = quizBundleService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!rebuildOnStartup) {
            return;
        }
        log.warn("[FreemiumRebuild] app.freemium-rebuild-on-startup=true — deleting issued bundles and reseeding");
        FreemiumRebuildResponse result = quizBundleService.rebuildFreemiumCatalog();
        log.warn("[FreemiumRebuild] Deleted {} issued bundles; seeded {} classes: {}",
                result.issuedBundlesDeleted(), result.classesSeeded(), result.classNames());
    }
}
