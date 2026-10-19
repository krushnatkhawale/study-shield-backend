package com.studyshield.studyshield.content.academicstructure;

import com.studyshield.studyshield.content.academicstructure.dto.PurgeReport;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.ContentPackRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.content.repository.QuizBundleRepository;
import com.studyshield.studyshield.content.repository.QuizRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deletes academic-structure <strong>rows</strong> (never tables, never the database)
 * so the empty-before-load guard passes. Order is mandatory: children before parents,
 * and — for {@code all} — the content chain first, because content packs and quiz
 * bundles hold FKs to offerings. Content reseeds from the question bank at the next
 * startup (or via {@code POST /quiz-bundles/rebuild-catalog}); attempt history keeps
 * dangling ids, exactly as the existing startup transition accepts.
 * <p>
 * Single-entity purge refuses (409) while child rows still reference the target —
 * no cascades, no surprises. {@code all} walks the full order in one pass.
 */
@Service
public class AcademicStructurePurgeService {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructurePurgeService.class);

    /** Content chain, children first — FKs to offerings block a bare structure purge. */
    private static final List<String> CONTENT_CHAIN = List.of(
            "questions", "quizzes", "content_packs", "quiz_bundles");

    private final TransactionTemplate transactionTemplate;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizBundleRepository quizBundleRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final BoardClassRepository boardClassRepository;
    private final BoardRepository boardRepository;
    private final CountryRepository countryRepository;
    private final ClassLevelRepository classLevelRepository;

    public AcademicStructurePurgeService(
            TransactionTemplate transactionTemplate,
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            ContentPackRepository contentPackRepository,
            QuizBundleRepository quizBundleRepository,
            BoardClassSubjectRepository boardClassSubjectRepository,
            SubjectRepository subjectRepository,
            BoardClassRepository boardClassRepository,
            BoardRepository boardRepository,
            CountryRepository countryRepository,
            ClassLevelRepository classLevelRepository) {
        this.transactionTemplate = transactionTemplate;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.contentPackRepository = contentPackRepository;
        this.quizBundleRepository = quizBundleRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.boardClassRepository = boardClassRepository;
        this.boardRepository = boardRepository;
        this.countryRepository = countryRepository;
        this.classLevelRepository = classLevelRepository;
    }

    /** Purge one entity (rows only) or all — content chain first, then reverse FK order. */
    public PurgeReport purge(String entityKey, boolean confirm) {
        if (!confirm) {
            throw new IllegalArgumentException(
                    "Purge requires explicit confirmation: add ?confirm=true");
        }
        List<PurgeReport.PurgeEntityResult> results = new ArrayList<>();
        if (entityKey == null || entityKey.isBlank() || "all".equalsIgnoreCase(entityKey.trim())) {
            purgeAll(results);
        } else {
            AcademicStructureEntity entity = AcademicStructureEntity.fromKey(entityKey)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown academic-structure entity '" + entityKey + "'"));
            purgeSingle(entity, results);
        }
        log.warn("[AcademicData] Purge {} ({} tables, {} rows)", entityKey == null ? "all" : entityKey,
                results.size(), results.stream().mapToLong(PurgeReport.PurgeEntityResult::rowsDeleted).sum());
        return new PurgeReport("ok", results);
    }

    private void purgeAll(List<PurgeReport.PurgeEntityResult> results) {
        // Content chain first — quiz_bundles/content_packs FK to offerings.
        purgeTable("questions", questionRepository::deleteAllInBatch, questionRepository::count, results);
        purgeTable("quizzes", quizRepository::deleteAllInBatch, quizRepository::count, results);
        purgeTable("content_packs", contentPackRepository::deleteAllInBatch, contentPackRepository::count, results);
        purgeTable("quiz_bundles", quizBundleRepository::deleteAllInBatch, quizBundleRepository::count, results);
        // Structure in reverse FK order.
        purgeTable("board_class_subject", boardClassSubjectRepository::deleteAllInBatch,
                boardClassSubjectRepository::count, results);
        purgeTable("subjects", subjectRepository::deleteAllInBatch, subjectRepository::count, results);
        purgeTable("board_class", boardClassRepository::deleteAllInBatch, boardClassRepository::count, results);
        purgeTable("boards", boardRepository::deleteAllInBatch, boardRepository::count, results);
        purgeTable("countries", countryRepository::deleteAllInBatch, countryRepository::count, results);
        purgeTable("class_levels", classLevelRepository::deleteAllInBatch, classLevelRepository::count, results);
    }

    private void purgeSingle(AcademicStructureEntity entity, List<PurgeReport.PurgeEntityResult> results) {
        Map<String, Long> blockers = blockingRows(entity);
        if (!blockers.isEmpty()) {
            List<Map<String, Object>> blocking = blockers.entrySet().stream()
                    .map(e -> Map.<String, Object>of("table", e.getKey(), "rows", e.getValue()))
                    .toList();
            String referencingTables = String.join(", ", blockers.keySet());
            throw new AcademicStructureConflictException(
                    "Rows still reference " + entity.key() + " (" + referencingTables
                            + "); delete the referencing rows first"
                            + " (or purge all).",
                    Map.of("status", "conflict", "entity", entity.key(), "blockingTables", blocking));
        }
        switch (entity) {
            case OFFERINGS -> purgeTable("board_class_subject", boardClassSubjectRepository::deleteAllInBatch,
                    boardClassSubjectRepository::count, results);
            case SUBJECTS -> purgeTable("subjects", subjectRepository::deleteAllInBatch,
                    subjectRepository::count, results);
            case BOARD_CLASSES -> purgeTable("board_class", boardClassRepository::deleteAllInBatch,
                    boardClassRepository::count, results);
            case BOARDS -> purgeTable("boards", boardRepository::deleteAllInBatch,
                    boardRepository::count, results);
            case COUNTRIES -> purgeTable("countries", countryRepository::deleteAllInBatch,
                    countryRepository::count, results);
            case CLASS_LEVELS -> purgeTable("class_levels", classLevelRepository::deleteAllInBatch,
                    classLevelRepository::count, results);
        }
    }

    /** Child rows that would block a single-entity delete (FK or logical dependency). */
    private Map<String, Long> blockingRows(AcademicStructureEntity entity) {
        Map<String, Long> blockers = new LinkedHashMap<>();
        switch (entity) {
            case CLASS_LEVELS -> {
                long n = boardClassRepository.count();
                if (n > 0) blockers.put("board_class", n);
            }
            case COUNTRIES -> {
                long n = boardRepository.count();
                if (n > 0) blockers.put("boards", n);
            }
            case BOARDS -> {
                long n = boardClassRepository.count();
                if (n > 0) blockers.put("board_class", n);
            }
            case BOARD_CLASSES -> {
                long n = boardClassSubjectRepository.count();
                if (n > 0) blockers.put("board_class_subject", n);
            }
            case SUBJECTS -> {
                long n = boardClassSubjectRepository.count();
                if (n > 0) blockers.put("board_class_subject", n);
            }
            case OFFERINGS -> {
                long packs = contentPackRepository.count();
                if (packs > 0) blockers.put("content_packs", packs);
                long bundles = quizBundleRepository.count();
                if (bundles > 0) blockers.put("quiz_bundles", bundles);
            }
        }
        return blockers;
    }

    private void purgeTable(String table,
                            Runnable deleteAll,
                            java.util.function.LongSupplier count,
                            List<PurgeReport.PurgeEntityResult> results) {
        long rows = count.getAsLong();
        if (rows == 0) {
            results.add(new PurgeReport.PurgeEntityResult(table, 0));
            return;
        }
        transactionTemplate.executeWithoutResult(status -> deleteAll.run());
        results.add(new PurgeReport.PurgeEntityResult(table, rows));
    }
}
