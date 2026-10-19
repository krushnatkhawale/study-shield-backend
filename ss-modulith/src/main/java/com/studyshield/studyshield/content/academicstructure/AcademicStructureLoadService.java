package com.studyshield.studyshield.content.academicstructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.content.academicstructure.dto.BoardClassRow;
import com.studyshield.studyshield.content.academicstructure.dto.BoardRow;
import com.studyshield.studyshield.content.academicstructure.dto.ClassLevelRow;
import com.studyshield.studyshield.content.academicstructure.dto.LoadReport;
import com.studyshield.studyshield.content.academicstructure.dto.OfferingRow;
import com.studyshield.studyshield.content.academicstructure.dto.SubjectRow;
import com.studyshield.studyshield.content.academicstructure.dto.CountryRow;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Initializes the academic structure from JSON (GitHub at the pinned ref, or an
 * uploaded document — identical DTOs and validation either way).
 * <p>
 * Load is <strong>initialize-only</strong>: an empty-before-load guard refuses to
 * write when any target table already has rows (use DELETE first). Rows carry their
 * {@code id} so a reload keeps ids stable; after each entity's insert the identity
 * sequence is bumped so later app-generated ids never collide. Parents are resolved
 * by natural keys and missing parents are reported, never stub-created.
 */
@Service
public class AcademicStructureLoadService {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructureLoadService.class);

    private final AcademicStructureFileSource fileSource;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManagerFactory entityManagerFactory;

    private final ClassLevelRepository classLevelRepository;
    private final CountryRepository countryRepository;
    private final BoardRepository boardRepository;
    private final BoardClassRepository boardClassRepository;
    private final SubjectRepository subjectRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;

    public AcademicStructureLoadService(
            AcademicStructureFileSource fileSource,
            ObjectMapper objectMapper,
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate,
            EntityManagerFactory entityManagerFactory,
            ClassLevelRepository classLevelRepository,
            CountryRepository countryRepository,
            BoardRepository boardRepository,
            BoardClassRepository boardClassRepository,
            SubjectRepository subjectRepository,
            BoardClassSubjectRepository boardClassSubjectRepository) {
        this.fileSource = fileSource;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManagerFactory = entityManagerFactory;
        this.classLevelRepository = classLevelRepository;
        this.countryRepository = countryRepository;
        this.boardRepository = boardRepository;
        this.boardClassRepository = boardClassRepository;
        this.subjectRepository = subjectRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
    }

    /** Entities in FK order; {@code single} is null for an all-entity load. */
    public static List<AcademicStructureEntity> entitiesFor(String entityKey) {
        if (entityKey == null || entityKey.isBlank() || "all".equalsIgnoreCase(entityKey.trim())) {
            return List.of(AcademicStructureEntity.values());
        }
        return AcademicStructureEntity.fromKey(entityKey)
                .map(List::of)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown academic-structure entity '" + entityKey + "'. Valid: class-levels, countries,"
                                + " boards, board-classes, subjects, offerings, all"));
    }

    /**
     * Load one entity (or all, in FK order) from the configured source or from a
     * provided JSON document (array, or object keyed by entity for all).
     */
    public LoadReport load(String entityKey, boolean dryRun, JsonNode provided) {
        List<AcademicStructureEntity> entities = entitiesFor(entityKey);

        Map<AcademicStructureEntity, Long> currentCounts = currentCounts(entities);
        if (currentCounts.values().stream().anyMatch(c -> c > 0)) {
            List<Map<String, Object>> conflicts = new ArrayList<>();
            currentCounts.forEach((entity, count) -> {
                if (count > 0) {
                    conflicts.add(Map.of("entity", entity.key(), "currentRows", count));
                }
            });
            throw new AcademicStructureConflictException(
                    "Target tables are not empty; load is initialize-only. DELETE first, then load.",
                    Map.of("status", "conflict", "conflicts", conflicts));
        }

        Map<AcademicStructureEntity, ParsedBatch> batches = new LinkedHashMap<>();
        for (AcademicStructureEntity entity : entities) {
            JsonNode json = provided != null
                    ? providedFor(entity, provided)
                    : parseJson(fileSource.fetchJson(entity), entity);
            batches.put(entity, parseBatch(entity, json));
        }

        validateParents(batches);

        List<LoadReport.LoadEntityResult> results = new ArrayList<>();
        for (AcademicStructureEntity entity : entities) {
            ParsedBatch batch = batches.get(entity);
            long start = System.currentTimeMillis();
            if (!dryRun) {
                transactionTemplate.executeWithoutResult(status -> insertBatch(entity, batch));
            }
            results.add(new LoadReport.LoadEntityResult(
                    entity.key(),
                    provided != null ? "upload" : fileSource.describe(entity),
                    batch.rowsCreated(),
                    dryRun ? 0 : batch.rowsCreated(),
                    batch.rowsSkipped(),
                    System.currentTimeMillis() - start));
        }

        String source = provided != null ? "upload" : fileSource.describe();
        log.info("[AcademicData] Load {} (dryRun={}) source={} entities={}",
                entityKey == null ? "all" : entityKey, dryRun, source, results.size());
        return new LoadReport(dryRun ? "planned" : "ok", dryRun, source, results);
    }

    /* ------------------------------------------------------------ row counts */

    public Map<AcademicStructureEntity, Long> currentCounts(List<AcademicStructureEntity> entities) {
        Map<AcademicStructureEntity, Long> counts = new LinkedHashMap<>();
        for (AcademicStructureEntity entity : entities) {
            counts.put(entity, countRows(entity));
        }
        return counts;
    }

    public long countRows(AcademicStructureEntity entity) {
        return switch (entity) {
            case CLASS_LEVELS -> classLevelRepository.count();
            case COUNTRIES -> countryRepository.count();
            case BOARDS -> boardRepository.count();
            case BOARD_CLASSES -> boardClassRepository.count();
            case SUBJECTS -> subjectRepository.count();
            case OFFERINGS -> boardClassSubjectRepository.count();
        };
    }

    /* --------------------------------------------------------------- parsing */

    private JsonNode parseJson(String json, AcademicStructureEntity entity) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON for " + entity.key() + ": " + ex.getMessage(), ex);
        }
    }

    /** Accepts a bare array, or the export wrapper {@code {"data": [...]}}. */
    private JsonNode providedFor(AcademicStructureEntity entity, JsonNode provided) {
        if (provided.isArray()) {
            return provided;
        }
        if (provided.isObject()) {
            if (provided.has("data") && provided.get("data").isArray()) {
                return provided.get("data");
            }
            if (provided.has(entity.key()) && provided.get(entity.key()).isArray()) {
                return provided.get(entity.key());
            }
        }
        throw new IllegalArgumentException(
                "Uploaded JSON must be an array of rows (or an object with a 'data' array) for " + entity.key());
    }

    private ParsedBatch parseBatch(AcademicStructureEntity entity, JsonNode rows) {
        if (!rows.isArray()) {
            throw new IllegalArgumentException("JSON for " + entity.key() + " must be an array of rows");
        }
        List<ClassLevelRow> classLevels = new ArrayList<>();
        List<CountryRow> countries = new ArrayList<>();
        List<BoardRow> boards = new ArrayList<>();
        List<BoardClassRow> boardClasses = new ArrayList<>();
        List<SubjectRow> subjects = new ArrayList<>();
        List<OfferingRow> offerings = new ArrayList<>();
        Set<String> naturalKeys = new HashSet<>();
        int skipped = 0;
        for (JsonNode node : rows) {
            String naturalKey;
            switch (entity) {
                case CLASS_LEVELS -> {
                    ClassLevelRow row = objectMapper.convertValue(node, ClassLevelRow.class);
                    parseStage(row.stage());
                    naturalKey = "ordinal:" + row.ordinal();
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    classLevels.add(row);
                }
                case COUNTRIES -> {
                    CountryRow row = objectMapper.convertValue(node, CountryRow.class);
                    naturalKey = "iso2:" + requireKey(row.iso2(), "iso2", entity);
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    countries.add(row);
                }
                case BOARDS -> {
                    BoardRow row = objectMapper.convertValue(node, BoardRow.class);
                    naturalKey = "code:" + requireKey(row.code(), "code", entity);
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    boards.add(row);
                }
                case BOARD_CLASSES -> {
                    BoardClassRow row = objectMapper.convertValue(node, BoardClassRow.class);
                    naturalKey = requireKey(row.boardCode(), "boardCode", entity) + "@" + row.classOrdinal();
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    boardClasses.add(row);
                }
                case SUBJECTS -> {
                    SubjectRow row = objectMapper.convertValue(node, SubjectRow.class);
                    naturalKey = "code:" + requireKey(row.code(), "code", entity);
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    subjects.add(row);
                }
                case OFFERINGS -> {
                    OfferingRow row = objectMapper.convertValue(node, OfferingRow.class);
                    naturalKey = requireKey(row.boardCode(), "boardCode", entity) + "@" + row.classOrdinal()
                            + "/" + requireKey(row.subjectCode(), "subjectCode", entity);
                    if (!naturalKeys.add(naturalKey)) { skipped++; continue; }
                    offerings.add(row);
                }
            }
        }
        return new ParsedBatch(classLevels, countries, boards, boardClasses, subjects, offerings,
                rows.size() - skipped, skipped);
    }

    private static String requireKey(String value, String field, AcademicStructureEntity entity) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(entity.key() + " row is missing required field '" + field + "'");
        }
        return value.trim();
    }

    /* ------------------------------------------------------- parent validation */

    /**
     * Parents must already exist (in the database or earlier in the same batch);
     * missing parents are reported, never auto-created.
     */
    private void validateParents(Map<AcademicStructureEntity, ParsedBatch> batches) {
        Set<String> countries = new HashSet<>();
        countryRepository.findAll().forEach(c -> countries.add(c.getIso2()));
        Set<String> boards = new HashSet<>();
        boardRepository.findAll().forEach(b -> boards.add(b.getCode()));
        Set<Integer> ordinals = new HashSet<>();
        classLevelRepository.findAll().forEach(l -> ordinals.add(l.getOrdinal()));
        Set<String> boardClasses = new HashSet<>();
        boardClassRepository.findAll().forEach(bc ->
                boardClasses.add(bc.getBoard().getCode() + "@" + bc.getClassLevel().getOrdinal()));
        Set<String> subjects = new HashSet<>();
        subjectRepository.findAll().forEach(s -> subjects.add(s.getCode()));

        // Fold the batches in FK order so parents earlier in the same load satisfy children.
        Map<String, List<String>> missing = new LinkedHashMap<>();
        for (Map.Entry<AcademicStructureEntity, ParsedBatch> entry : batches.entrySet()) {
            AcademicStructureEntity entity = entry.getKey();
            ParsedBatch batch = entry.getValue();
            switch (entity) {
                case CLASS_LEVELS -> batch.classLevels().forEach(l -> ordinals.add(l.ordinal()));
                case COUNTRIES -> batch.countries().forEach(c -> countries.add(c.iso2().trim()));
                case BOARDS -> {
                    for (BoardRow row : batch.boards()) {
                        if (row.countryIso2() != null && !row.countryIso2().isBlank()
                                && !countries.contains(row.countryIso2().trim())) {
                            missing.computeIfAbsent("countries", k -> new ArrayList<>())
                                    .add(row.countryIso2().trim() + " (referenced by board " + row.code() + ")");
                        }
                    }
                    batch.boards().forEach(b -> boards.add(b.code().trim()));
                }
                case BOARD_CLASSES -> {
                    for (BoardClassRow row : batch.boardClasses()) {
                        if (!boards.contains(row.boardCode().trim())) {
                            missing.computeIfAbsent("boards", k -> new ArrayList<>()).add(row.boardCode().trim());
                        }
                        if (!ordinals.contains(row.classOrdinal())) {
                            missing.computeIfAbsent("class-levels", k -> new ArrayList<>())
                                    .add("ordinal " + row.classOrdinal());
                        }
                    }
                    batch.boardClasses().forEach(bc ->
                            boardClasses.add(bc.boardCode().trim() + "@" + bc.classOrdinal()));
                }
                case SUBJECTS -> batch.subjects().forEach(s -> subjects.add(s.code().trim()));
                case OFFERINGS -> {
                    for (OfferingRow row : batch.offerings()) {
                        if (!boardClasses.contains(row.boardCode().trim() + "@" + row.classOrdinal())) {
                            missing.computeIfAbsent("board-classes", k -> new ArrayList<>())
                                    .add(row.boardCode().trim() + "@" + row.classOrdinal());
                        }
                        if (!subjects.contains(row.subjectCode().trim())) {
                            missing.computeIfAbsent("subjects", k -> new ArrayList<>()).add(row.subjectCode().trim());
                        }
                    }
                }
                default -> { /* leaf entities have no parents */ }
            }
        }
        if (!missing.isEmpty()) {
            List<Map<String, Object>> missingParents = missing.entrySet().stream()
                    .map(e -> Map.<String, Object>of(
                            "entity", e.getKey(),
                            "keys", e.getValue().stream().distinct().sorted().toList()))
                    .toList();
            throw new AcademicStructureConflictException(
                    "Parent rows absent; load parents first (or use load all).",
                    Map.of("status", "conflict", "missingParents", missingParents));
        }
    }

    /* ----------------------------------------------------------------- insert */

    /**
     * Rows are inserted with explicit SQL so the file's {@code id} values are honored
     * verbatim — Hibernate's IDENTITY generation rejects explicit ids on both persist
     * and merge. Each entity runs in its own transaction; the identity sequence is
     * bumped past the max id right after the inserts.
     */
    private void insertBatch(AcademicStructureEntity entity, ParsedBatch batch) {
        String table = qualifiedTable(entity.tableName());
        switch (entity) {
            case CLASS_LEVELS -> {
                for (ClassLevelRow row : batch.classLevels()) {
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (id, ordinal, slug, canonical_name, age_min_years, age_max_years,"
                                        + " stage, notes) VALUES (?,?,?,?,?,?,?,?)",
                                row.id(), row.ordinal(), row.slug(), row.canonicalName(),
                                row.ageMinYears(), row.ageMaxYears(), row.stage(), row.notes());
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (ordinal, slug, canonical_name, age_min_years, age_max_years,"
                                        + " stage, notes) VALUES (?,?,?,?,?,?,?)",
                                row.ordinal(), row.slug(), row.canonicalName(),
                                row.ageMinYears(), row.ageMaxYears(), row.stage(), row.notes());
                    }
                }
            }
            case COUNTRIES -> {
                for (CountryRow row : batch.countries()) {
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table + " (id, iso2, name) VALUES (?,?,?)",
                                row.id(), row.iso2().trim(), row.name());
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table + " (iso2, name) VALUES (?,?)",
                                row.iso2().trim(), row.name());
                    }
                }
            }
            case BOARDS -> {
                for (BoardRow row : batch.boards()) {
                    Long countryId = null;
                    if (row.countryIso2() != null && !row.countryIso2().isBlank()) {
                        countryId = countryRepository.findByIso2(row.countryIso2().trim())
                                .orElseThrow(() -> new IllegalStateException(
                                        "Country " + row.countryIso2() + " vanished mid-load"))
                                .getId();
                    }
                    boolean active = row.active() == null || row.active();
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (id, country_id, name, code, description, min_ordinal, max_ordinal,"
                                        + " active) VALUES (?,?,?,?,?,?,?,?)",
                                row.id(), countryId, row.name(), row.code().trim(), row.description(),
                                row.minOrdinal(), row.maxOrdinal(), active);
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (country_id, name, code, description, min_ordinal, max_ordinal,"
                                        + " active) VALUES (?,?,?,?,?,?,?)",
                                countryId, row.name(), row.code().trim(), row.description(),
                                row.minOrdinal(), row.maxOrdinal(), active);
                    }
                }
            }
            case BOARD_CLASSES -> {
                for (BoardClassRow row : batch.boardClasses()) {
                    Long boardId = boardRepository.findByCode(row.boardCode().trim()).orElseThrow().getId();
                    Long levelId = classLevelRepository.findByOrdinal(row.classOrdinal()).orElseThrow().getId();
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (id, board_id, class_level_id, display_name) VALUES (?,?,?,?)",
                                row.id(), boardId, levelId, row.displayName());
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (board_id, class_level_id, display_name) VALUES (?,?,?)",
                                boardId, levelId, row.displayName());
                    }
                }
            }
            case SUBJECTS -> {
                for (SubjectRow row : batch.subjects()) {
                    boolean active = row.active() == null || row.active();
                    int displayOrder = row.displayOrder() == null ? 0 : row.displayOrder();
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (id, code, name, description, active, display_order) VALUES (?,?,?,?,?,?)",
                                row.id(), row.code().trim(), row.name(), row.description(), active, displayOrder);
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (code, name, description, active, display_order) VALUES (?,?,?,?,?)",
                                row.code().trim(), row.name(), row.description(), active, displayOrder);
                    }
                }
            }
            case OFFERINGS -> {
                for (OfferingRow row : batch.offerings()) {
                    Long boardClassId = boardClassRepository
                            .findByBoard_IdAndClassLevel_Ordinal(
                                    boardRepository.findByCode(row.boardCode().trim()).orElseThrow().getId(),
                                    row.classOrdinal())
                            .orElseThrow().getId();
                    Long subjectId = subjectRepository.findByCode(row.subjectCode().trim()).orElseThrow().getId();
                    if (row.id() != null) {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (id, board_class_id, subject_id) VALUES (?,?,?)",
                                row.id(), boardClassId, subjectId);
                    } else {
                        jdbcTemplate.update("INSERT INTO " + table
                                        + " (board_class_id, subject_id) VALUES (?,?)",
                                boardClassId, subjectId);
                    }
                }
            }
        }
        bumpIdentitySequence(entity);
    }

    private static ClassLevel.Stage parseStage(String stage) {
        if (stage == null || stage.isBlank()) {
            throw new IllegalArgumentException("class-levels row is missing required field 'stage'");
        }
        try {
            return ClassLevel.Stage.valueOf(stage.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown class-levels stage '" + stage
                    + "'; valid: EARLY_YEARS, PRIMARY, LOWER_SECONDARY, UPPER_SECONDARY");
        }
    }

    /**
     * After inserting explicit ids, move the identity sequence past the max id so
     * application-generated ids never collide. {@code ALTER COLUMN … RESTART WITH}
     * works for both PostgreSQL (serial/identity) and H2.
     */
    private void bumpIdentitySequence(AcademicStructureEntity entity) {
        String table = qualifiedTable(entity.tableName());
        Long maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM " + table, Long.class);
        long restartAt = (maxId == null ? 0L : maxId) + 1;
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + restartAt);
    }

    private String qualifiedTable(String table) {
        Object schema = entityManagerFactory.getProperties().get("hibernate.default_schema");
        if (schema == null || schema.toString().isBlank()) {
            return table;
        }
        String s = schema.toString().trim();
        return (s.startsWith("\"") ? s : "\"" + s + "\"") + "." + table;
    }

    /** Parsed + de-duplicated rows for one entity. */
    private record ParsedBatch(
            List<ClassLevelRow> classLevels,
            List<CountryRow> countries,
            List<BoardRow> boards,
            List<BoardClassRow> boardClasses,
            List<SubjectRow> subjects,
            List<OfferingRow> offerings,
            int rowsCreated,
            int rowsSkipped
    ) {}
}
