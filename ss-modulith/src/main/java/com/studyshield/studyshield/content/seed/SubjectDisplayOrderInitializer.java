package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Assign curated {@code displayOrder} values to legacy subjects that were seeded before the
 * column existed (all created with the 0 default). This runs on startup and is idempotent:
 * subjects whose displayOrder is already non-zero are left untouched.
 * <p>
 * The order drives {@code QuizBundleService} subject selection (first {@code QUIZZES_PER_CLASS}
 * subjects win the freemium bundle), so this puts Math first, then EVS/English/Hindi, with
 * everything else (e.g. legacy "General Knowledge") behind them — making uploaded bank quizzes
 * surface instead of legacy GK+Math.
 */
@Component
public class SubjectDisplayOrderInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SubjectDisplayOrderInitializer.class);

    private static final Map<String, Integer> CURATED_ORDER = Map.of(
            "math", 1,
            "evs", 2,
            "english", 3,
            "hindi", 4,
            "general knowledge", 5
    );
    private static final int LEGACY_FALLBACK_ORDER = 10;

    private final SubjectRepository subjectRepository;

    public SubjectDisplayOrderInitializer(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Subject> subjects = subjectRepository.findAll().stream()
                .filter(s -> s.getDisplayOrder() == 0)
                .toList();
        if (subjects.isEmpty()) {
            return;
        }
        int updated = 0;
        for (Subject subject : subjects) {
            int order = CURATED_ORDER.getOrDefault(
                    subject.getName() == null ? "" : subject.getName().toLowerCase(Locale.ROOT).trim(),
                    LEGACY_FALLBACK_ORDER);
            subject.setDisplayOrder(order);
            updated++;
        }
        subjectRepository.saveAll(subjects);
        log.info("[SubjectOrder] Assigned displayOrder to {} existing subjects", updated);
    }
}
