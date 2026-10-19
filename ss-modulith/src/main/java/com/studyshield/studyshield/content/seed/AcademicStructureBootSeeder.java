package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.academicstructure.AcademicStructureLoadService;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Regression/CI bootstrap for the academic structure: loads the matrix from
 * {@code classpath:academic-structure/*.json} through the real
 * {@link AcademicStructureLoadService} (same parse → validate → insert →
 * bump-sequence path production uses). Gated by
 * {@code app.academic-structure-seeding.enabled} — only the {@code regression}
 * profile sets it; production never does (structure arrives via
 * {@code POST /api/v1/academic-structure/load} from GitHub). Idempotent: skips when
 * the structure already has rows.
 */
@Component
@Order(1)
public class AcademicStructureBootSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructureBootSeeder.class);

    private final AcademicStructureLoadService loadService;
    private final ClassLevelRepository classLevelRepository;

    @Value("${app.academic-structure-seeding.enabled:false}")
    private boolean seedingEnabled;

    public AcademicStructureBootSeeder(AcademicStructureLoadService loadService,
                                       ClassLevelRepository classLevelRepository) {
        this.loadService = loadService;
        this.classLevelRepository = classLevelRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedingEnabled) {
            return;
        }
        if (classLevelRepository.count() > 0) {
            log.info("[AcademicStructureSeed] Structure already loaded — skipping");
            return;
        }
        long start = System.currentTimeMillis();
        loadService.load(null, false, null);
        log.info("[AcademicStructureSeed] Matrix loaded from classpath in {}ms",
                System.currentTimeMillis() - start);
    }
}
