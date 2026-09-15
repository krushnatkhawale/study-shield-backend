package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.Offering;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.OfferingRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seeds the academic structure (class_levels, boards, board_class display names,
 * global subjects, board_class_subject offerings).
 * Idempotent: every insert is guarded by an existence check, safe to run on each startup.
 *
 * <p>Subjects are global (no classGrade): MATH/ENG/EVS/SCI/SST/HI/OTHER.
 * Offerings (board_class_subject) act as the availability switchboard: CBSE+ALL
 * board classes get MATH/ENG/EVS for ordinals 2-16, plus SCI/SST from ordinal 10+.
 */
@Component
public class AcademicStructureSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructureSeeder.class);

    private record Level(int ordinal, String slug, String canonicalName,
                         double ageMin, double ageMax, String stage) {}

    // 17-level owner spec (ordinals 1-17; ordinal 17 is board-specific overflow).
    private static final List<Level> LEVELS = List.of(
            new Level(1, "playgroup", "Playgroup", 1.5, 3, "early_years"),
            new Level(2, "nursery", "Nursery", 3, 4, "early_years"),
            new Level(3, "junior-kg", "Junior KG", 4, 5, "early_years"),
            new Level(4, "senior-kg", "Senior KG", 5, 6, "early_years"),
            new Level(5, "class-1", "Class 1", 6, 7, "primary"),
            new Level(6, "class-2", "Class 2", 7, 8, "primary"),
            new Level(7, "class-3", "Class 3", 8, 9, "primary"),
            new Level(8, "class-4", "Class 4", 9, 10, "primary"),
            new Level(9, "class-5", "Class 5", 10, 11, "primary"),
            new Level(10, "class-6", "Class 6", 11, 12, "lower_secondary"),
            new Level(11, "class-7", "Class 7", 12, 13, "lower_secondary"),
            new Level(12, "class-8", "Class 8", 13, 14, "lower_secondary"),
            new Level(13, "class-9", "Class 9", 14, 15, "lower_secondary"),
            new Level(14, "class-10", "Class 10", 15, 16, "lower_secondary"),
            new Level(15, "class-11", "Class 11", 16, 17, "upper_secondary"),
            new Level(16, "class-12", "Class 12", 17, 18, "upper_secondary"),
            new Level(17, "upper-year-5", "Level 17 (board-specific)", 18, 19, "upper_secondary"));

    private record BoardDef(String code, String name) {}

    private record SubjectDef(String code, String name, int displayOrder) {}

    private static final List<SubjectDef> GLOBAL_SUBJECTS = List.of(
            new SubjectDef("MATH", "Mathematics", 1),
            new SubjectDef("ENG", "English", 2),
            new SubjectDef("EVS", "Environmental Studies", 3),
            new SubjectDef("SCI", "Science", 4),
            new SubjectDef("SST", "Social Studies", 5),
            new SubjectDef("HI", "Hindi", 6),
            new SubjectDef("OTHER", "Other", 7));

    // IB appears only in a V7-file comment; the owner seed spec has no IB row, so it is not seeded.
    private static final List<BoardDef> BOARDS = List.of(
            new BoardDef("ALL", "Generic All Boards"),
            new BoardDef("CBSE", "Central Board of Secondary Education"),
            new BoardDef("MH", "Maharashtra State Board"),
            new BoardDef("ICSE", "Indian Certificate of Secondary Education"),
            new BoardDef("ENG", "England"),
            new BoardDef("US", "United States"));

    private final ClassLevelRepository classLevels;
    private final BoardRepository boards;
    private final BoardClassRepository boardClasses;
    private final SubjectRepository subjects;
    private final OfferingRepository offerings;

    @Value("${app.academic-seeding.enabled:true}")
    private boolean seedingEnabled;

    public AcademicStructureSeeder(ClassLevelRepository classLevels, BoardRepository boards,
                                   BoardClassRepository boardClasses, SubjectRepository subjects,
                                   OfferingRepository offerings) {
        this.classLevels = classLevels;
        this.boards = boards;
        this.boardClasses = boardClasses;
        this.subjects = subjects;
        this.offerings = offerings;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedingEnabled) {
            log.info("[AcademicSeed] Disabled via app.academic-seeding.enabled=false — skipping");
            return;
        }
        for (Level l : LEVELS) {
            if (classLevels.existsByOrdinal(l.ordinal()) || classLevels.existsBySlug(l.slug())) {
                continue;
            }
            ClassLevel entity = new ClassLevel();
            entity.setOrdinal(l.ordinal());
            entity.setSlug(l.slug());
            entity.setCanonicalName(l.canonicalName());
            entity.setAgeMinYears(l.ageMin());
            entity.setAgeMaxYears(l.ageMax());
            entity.setStage(l.stage());
            classLevels.save(entity);
        }
        for (BoardDef b : BOARDS) {
            if (boards.existsByCode(b.code())) {
                continue;
            }
            boards.save(Board.builder().code(b.code()).name(b.name()).active(true).build());
        }
        for (BoardDef b : BOARDS) {
            Board board = boards.findByCode(b.code()).orElseThrow();
            Set<String> existing = boardClasses.findByBoardId(board.getId()).stream()
                    .map(bc -> bc.getClassLevel().getId() + "#" + bc.getDisplayName())
                    .collect(Collectors.toSet());
            for (ClassLevel level : classLevels.findAll()) {
                String displayName = displayName(b.code(), level.getOrdinal());
                if (displayName == null) {
                    continue; // ordinal outside this board's range (e.g. ENG/US skip playgroup, ordinal 17)
                }
                if (!existing.contains(level.getId() + "#" + displayName)) {
                    BoardClass bc = new BoardClass();
                    bc.setBoard(board);
                    bc.setClassLevel(level);
                    bc.setDisplayName(displayName);
                    boardClasses.save(bc);
                }
            }
        }
        log.info("[AcademicSeed] Academic structure seeding complete");
        seedGlobalSubjects();
        seedOfferings();
        log.info("[AcademicSeed] Subject/offering seeding complete");
    }

    private void seedGlobalSubjects() {
        for (SubjectDef s : GLOBAL_SUBJECTS) {
            if (subjects.existsByCodeAndClassGradeIsNull(s.code())) {
                continue;
            }
            subjects.save(Subject.builder().code(s.code()).name(s.name())
                    .active(true).displayOrder(s.displayOrder()).build());
        }
    }

    private void seedOfferings() {
        for (String boardCode : List.of("CBSE", "ALL")) {
            Board board = boards.findByCode(boardCode).orElse(null);
            if (board == null) {
                continue;
            }
            for (BoardClass bc : boardClasses.findByBoardId(board.getId())) {
                int ordinal = bc.getClassLevel().getOrdinal();
                if (ordinal < 2 || ordinal > 16) {
                    continue;
                }
                List<String> codes = ordinal >= 10
                        ? List.of("MATH", "ENG", "EVS", "SCI", "SST")
                        : List.of("MATH", "ENG", "EVS");
                for (String code : codes) {
                    Subject subject = subjects.findByCodeAndClassGradeIsNull(code).orElse(null);
                    if (subject == null
                            || offerings.existsByBoardClassIdAndSubjectId(bc.getId(), subject.getId())) {
                        continue;
                    }
                    Offering offering = new Offering();
                    offering.setBoardClass(bc);
                    offering.setSubject(subject);
                    offerings.save(offering);
                }
            }
        }
    }

    /** Display names per owner spec: India names, ALL fallback, ENG Year mapping, US Grade mapping. */
    private static String displayName(String boardCode, int ordinal) {
        return switch (boardCode) {
            case "ENG" -> switch (ordinal) {
                case 2, 3 -> "Nursery";
                case 4 -> "Reception";
                default -> (ordinal >= 5 && ordinal <= 16) ? "Year " + (ordinal - 4) : null;
            };
            case "US" -> switch (ordinal) {
                case 2, 3 -> "Preschool";
                case 4 -> "Kindergarten";
                default -> (ordinal >= 5 && ordinal <= 16) ? "Grade " + (ordinal - 4) : null;
            };
            default -> switch (ordinal) { // ALL, CBSE, MH, ICSE share India names
                case 1 -> "Playgroup";
                case 2 -> "Nursery";
                case 3 -> "Junior KG";
                case 4 -> "Senior KG";
                default -> (ordinal >= 5 && ordinal <= 16) ? "Class " + (ordinal - 4) : null;
            };
        };
    }
}
