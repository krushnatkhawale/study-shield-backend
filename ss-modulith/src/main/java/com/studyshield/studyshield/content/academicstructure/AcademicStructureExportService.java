package com.studyshield.studyshield.content.academicstructure;

import com.studyshield.studyshield.content.academicstructure.dto.BoardClassRow;
import com.studyshield.studyshield.content.academicstructure.dto.BoardRow;
import com.studyshield.studyshield.content.academicstructure.dto.ClassLevelRow;
import com.studyshield.studyshield.content.academicstructure.dto.OfferingRow;
import com.studyshield.studyshield.content.academicstructure.dto.SubjectRow;
import com.studyshield.studyshield.content.academicstructure.dto.CountryRow;
import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.BoardClassSubject;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.Country;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes the current structure rows back into the exact JSON schema the load
 * endpoint consumes (same DTOs, sorted by id) — the round-trip invariant that keeps
 * {@code load(export(x))} exact and git diffs clean. Export on empty tables is a 409:
 * there is nothing to back up.
 */
@Service
@Transactional(readOnly = true)
public class AcademicStructureExportService {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructureExportService.class);

    private final AcademicStructureLoadService loadService;
    private final ClassLevelRepository classLevelRepository;
    private final CountryRepository countryRepository;
    private final BoardRepository boardRepository;
    private final BoardClassRepository boardClassRepository;
    private final SubjectRepository subjectRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;

    public AcademicStructureExportService(
            AcademicStructureLoadService loadService,
            ClassLevelRepository classLevelRepository,
            CountryRepository countryRepository,
            BoardRepository boardRepository,
            BoardClassRepository boardClassRepository,
            SubjectRepository subjectRepository,
            BoardClassSubjectRepository boardClassSubjectRepository) {
        this.loadService = loadService;
        this.classLevelRepository = classLevelRepository;
        this.countryRepository = countryRepository;
        this.boardRepository = boardRepository;
        this.boardClassRepository = boardClassRepository;
        this.subjectRepository = subjectRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
    }

    /** Export all entities in FK order, keyed by entity name. */
    public Map<String, List<?>> exportAll() {
        Map<AcademicStructureEntity, Long> counts =
                loadService.currentCounts(List.of(AcademicStructureEntity.values()));
        requireNonEmpty(counts);
        Map<String, List<?>> out = new LinkedHashMap<>();
        for (AcademicStructureEntity entity : AcademicStructureEntity.values()) {
            out.put(entity.key(), exportEntity(entity));
        }
        log.info("[AcademicData] Export all: {}", counts);
        return out;
    }

    /** Export one entity as a list of rows in the load file schema, sorted by id. */
    public List<?> exportEntity(AcademicStructureEntity entity) {
        if (loadService.countRows(entity) == 0) {
            requireNonEmpty(Map.of(entity, 0L));
        }
        return switch (entity) {
            case CLASS_LEVELS -> classLevelRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(ClassLevel::getId))
                    .map(l -> new ClassLevelRow(l.getId(), l.getOrdinal(), l.getSlug(), l.getCanonicalName(),
                            l.getAgeMinYears(), l.getAgeMaxYears(), l.getStage().name(), l.getNotes()))
                    .toList();
            case COUNTRIES -> countryRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(Country::getId))
                    .map(c -> new CountryRow(c.getId(), c.getIso2(), c.getName()))
                    .toList();
            case BOARDS -> boardRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(Board::getId))
                    .map(b -> new BoardRow(
                            b.getId(),
                            b.getCountry() == null ? null : b.getCountry().getIso2(),
                            b.getName(), b.getCode(), b.getDescription(),
                            b.getMinOrdinal(), b.getMaxOrdinal(), b.isActive()))
                    .toList();
            case BOARD_CLASSES -> boardClassRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(BoardClass::getId))
                    .map(bc -> new BoardClassRow(
                            bc.getId(),
                            bc.getBoard().getCode(),
                            bc.getClassLevel().getOrdinal(),
                            bc.getDisplayName()))
                    .toList();
            case SUBJECTS -> subjectRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(Subject::getId))
                    .map(s -> new SubjectRow(s.getId(), s.getCode(), s.getName(), s.getDescription(),
                            s.isActive(), s.getDisplayOrder()))
                    .toList();
            case OFFERINGS -> boardClassSubjectRepository.findAll().stream()
                    .sorted(java.util.Comparator.comparing(BoardClassSubject::getId))
                    .map(o -> new OfferingRow(
                            o.getId(),
                            o.getBoardClass().getBoard().getCode(),
                            o.getBoardClass().getClassLevel().getOrdinal(),
                            o.getSubject().getCode()))
                    .toList();
        };
    }

    private static void requireNonEmpty(Map<AcademicStructureEntity, Long> counts) {
        List<Map<String, Object>> conflicts = counts.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(e -> Map.<String, Object>of("entity", e.getKey().key(), "currentRows", 0L))
                .toList();
        if (!conflicts.isEmpty()) {
            throw new AcademicStructureConflictException(
                    "Nothing to export: target tables are empty.",
                    Map.of("status", "conflict", "conflicts", conflicts));
        }
    }
}
