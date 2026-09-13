package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.BoardClassSubject;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.ClassLevel.Stage;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the global academic matrix on startup (runs before every other seeding
 * runner via {@code @Order(1)}):
 * <p>
 *   {@code class_levels} (17 global ordinals), {@code countries}, {@code boards},
 *   {@code board_class} (each board's labels per ordinal), global {@code subjects},
 *   and the {@code board_class_subject} offerings.
 * <p>
 * Offerings are grid-anchored (board + ordinal + global subject), never display names.
 * v1 seeds offerings only for the generic ALL board and CBSE:
 * Math/English/EVS on every covered ordinal, plus Science/Social Studies from ordinal
 * ≥ 10 (Class 6 up). Other boards get labels only — no offerings — so their content is
 * never mixed in until explicitly added.
 * <p>
 * {@code Trial / Exp} are NOT class levels here: they resolve to Nursery (ordinal 2).
 * <p>
 * Idempotent: existing rows are left untouched, so reboots never duplicate or clobber.
 */
@Component
@Order(1)
public class AcademicStructureSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AcademicStructureSeeder.class);

    static final int MIN_OFFER_ORDINAL_SCI_SST = 10;

    private final ClassLevelRepository classLevelRepository;
    private final CountryRepository countryRepository;
    private final BoardRepository boardRepository;
    private final BoardClassRepository boardClassRepository;
    private final SubjectRepository subjectRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;

    @Value("${app.academic-structure-seeding.enabled:true}")
    private boolean seedingEnabled;

    public AcademicStructureSeeder(
            ClassLevelRepository classLevelRepository,
            CountryRepository countryRepository,
            BoardRepository boardRepository,
            BoardClassRepository boardClassRepository,
            SubjectRepository subjectRepository,
            BoardClassSubjectRepository boardClassSubjectRepository
    ) {
        this.classLevelRepository = classLevelRepository;
        this.countryRepository = countryRepository;
        this.boardRepository = boardRepository;
        this.boardClassRepository = boardClassRepository;
        this.subjectRepository = subjectRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedingEnabled) {
            log.info("[AcademicStructure] Disabled via app.academic-structure-seeding.enabled=false — skipping");
            return;
        }
        long start = System.currentTimeMillis();
        seedClassLevels();
        Map<String, Country> countries = seedCountries();
        Map<String, Board> boards = seedBoards(countries);
        Map<String, Subject> subjects = seedSubjects();
        seedBoardClasses(boards);
        seedOfferings(boards, subjects);
        log.info("[AcademicStructure] Matrix seeded in {}ms", System.currentTimeMillis() - start);
    }

    private void seedClassLevels() {
        Object[][] levels = {
                {1, "playgroup", "Playgroup", "1.5", "3.0", Stage.EARLY_YEARS, "Foundation/Nursery play age"},
                {2, "nursery", "Nursery", "3.0", "4.0", Stage.EARLY_YEARS, "Trial/Exp resolve here"},
                {3, "junior-kg", "Junior KG", "4.0", "5.0", Stage.EARLY_YEARS, "LKG"},
                {4, "senior-kg", "Senior KG", "5.0", "6.0", Stage.EARLY_YEARS, "UKG/Reception/Kindergarten"},
                {5, "class-1", "Class 1", "6.0", "7.0", Stage.PRIMARY, null},
                {6, "class-2", "Class 2", "7.0", "8.0", Stage.PRIMARY, null},
                {7, "class-3", "Class 3", "8.0", "9.0", Stage.PRIMARY, null},
                {8, "class-4", "Class 4", "9.0", "10.0", Stage.PRIMARY, null},
                {9, "class-5", "Class 5", "10.0", "11.0", Stage.PRIMARY, null},
                {10, "class-6", "Class 6", "11.0", "12.0", Stage.LOWER_SECONDARY, null},
                {11, "class-7", "Class 7", "12.0", "13.0", Stage.LOWER_SECONDARY, null},
                {12, "class-8", "Class 8", "13.0", "14.0", Stage.LOWER_SECONDARY, null},
                {13, "class-9", "Class 9", "14.0", "15.0", Stage.LOWER_SECONDARY, null},
                {14, "class-10", "Class 10", "15.0", "16.0", Stage.LOWER_SECONDARY, null},
                {15, "class-11", "Class 11", "16.0", "17.0", Stage.UPPER_SECONDARY, null},
                {16, "class-12", "Class 12", "17.0", "18.0", Stage.UPPER_SECONDARY, null},
                {17, "class-13", "Class 13", "18.0", "19.0", Stage.UPPER_SECONDARY, "Beyond India boards (ENG Y13)"},
        };
        for (Object[] row : levels) {
            int ordinal = (int) row[0];
            if (classLevelRepository.existsByOrdinal(ordinal)) {
                continue;
            }
            ClassLevel level = new ClassLevel();
            level.setOrdinal(ordinal);
            level.setSlug((String) row[1]);
            level.setCanonicalName((String) row[2]);
            level.setAgeMinYears(new BigDecimal((String) row[3]));
            level.setAgeMaxYears(new BigDecimal((String) row[4]));
            level.setStage((Stage) row[5]);
            level.setNotes((String) row[6]);
            classLevelRepository.save(level);
        }
    }

    private Map<String, Country> seedCountries() {
        Map<String, Country> countries = new LinkedHashMap<>();
        seedCountry(countries, "IN", "India");
        seedCountry(countries, "GB", "United Kingdom");
        seedCountry(countries, "US", "United States");
        return countries;
    }

    private void seedCountry(Map<String, Country> out, String iso2, String name) {
        if (!countryRepository.existsByIso2(iso2)) {
            out.put(iso2, countryRepository.save(new Country(iso2, name)));
        } else {
            out.put(iso2, countryRepository.findByIso2(iso2).orElseThrow());
        }
    }

    private Map<String, Board> seedBoards(Map<String, Country> countries) {
        Map<String, Board> boards = new LinkedHashMap<>();
        seedBoard(boards, "ALL", "All Boards", null, 2, 16, "Generic / board-agnostic common-core content");
        seedBoard(boards, "CBSE", "CBSE", countries.get("IN"), 1, 16, "Central Board of Secondary Education (India)");
        seedBoard(boards, "MH", "Maharashtra", countries.get("IN"), 1, 16, "Maharashtra State Board (India)");
        seedBoard(boards, "ICSE", "ICSE", countries.get("IN"), 1, 16, "CISCE (India)");
        seedBoard(boards, "ENG", "England", countries.get("GB"), 2, 16, "National Curriculum (England)");
        seedBoard(boards, "US", "USA", countries.get("US"), 2, 16, "Common Core (United States)");
        seedBoard(boards, "IB", "IB", null, 2, 16, "International Baccalaureate (global)");
        return boards;
    }

    private void seedBoard(Map<String, Board> out, String code, String name, Country country,
                           int minOrdinal, int maxOrdinal, String description) {
        Board board = boardRepository.findByCode(code).orElseGet(() -> {
            Board b = new Board();
            b.setName(name);
            b.setCode(code);
            b.setCountry(country);
            b.setDescription(description);
            b.setMinOrdinal(minOrdinal);
            b.setMaxOrdinal(maxOrdinal);
            b.setActive(true);
            return boardRepository.save(b);
        });
        out.put(code, board);
    }

    private Map<String, Subject> seedSubjects() {
        Map<String, Subject> subjects = new LinkedHashMap<>();
        subjects.put("MATH", seedSubject("MATH", "Math", 1, "Arithmetic / numeracy"));
        subjects.put("ENG", seedSubject("ENG", "English", 2, "English language"));
        subjects.put("EVS", seedSubject("EVS", "EVS", 3, "Environmental Studies"));
        subjects.put("SCI", seedSubject("SCI", "Science", 4, "General Science"));
        subjects.put("SST", seedSubject("SST", "Social Studies", 5, "History / Geography / Civics"));
        subjects.put("HI", seedSubject("HI", "Hindi", 6, "Hindi language"));
        subjects.put("OTHER", seedSubject("OTHER", "Other", 7, "Any other subject"));
        return subjects;
    }

    private Subject seedSubject(String code, String name, int displayOrder, String description) {
        return subjectRepository.findByCode(code).orElseGet(() -> {
            Subject subject = new Subject();
            subject.setCode(code);
            subject.setName(name);
            subject.setDescription(description);
            subject.setDisplayOrder(displayOrder);
            subject.setActive(true);
            return subjectRepository.save(subject);
        });
    }

    private void seedBoardClasses(Map<String, Board> boards) {
        for (Board board : boards.values()) {
            for (int ordinal = board.getMinOrdinal(); ordinal <= board.getMaxOrdinal(); ordinal++) {
                String displayName = India.displayName(ordinal);
                if (!"ALL".equals(board.getCode()) && !"CBSE".equals(board.getCode())
                        && !"MH".equals(board.getCode()) && !"ICSE".equals(board.getCode())) {
                    displayName = BoardLabels.displayName(board.getCode(), ordinal);
                }
                String name = displayName;
                if (boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(board.getId(), ordinal).isEmpty()) {
                    BoardClass bc = new BoardClass();
                    bc.setBoard(board);
                    bc.setClassLevel(ordinalLevel(ordinal));
                    bc.setDisplayName(name);
                    boardClassRepository.save(bc);
                }
            }
        }
    }

    private ClassLevel ordinalLevel(int ordinal) {
        return classLevelRepository.findByOrdinal(ordinal).orElseThrow();
    }

    private void seedOfferings(Map<String, Board> boards, Map<String, Subject> subjects) {
        List<String> offerBoards = List.of("ALL", "CBSE");
        for (String boardCode : offerBoards) {
            Board board = boards.get(boardCode);
            for (int ordinal = board.getMinOrdinal(); ordinal <= board.getMaxOrdinal(); ordinal++) {
                BoardClass boardClass = boardClassRepository
                        .findByBoard_IdAndClassLevel_Ordinal(board.getId(), ordinal)
                        .orElseThrow();
                for (String subjectCode : offerSubjectCodes(ordinal)) {
                    if (boardClassSubjectRepository
                            .existsByBoardClass_IdAndSubject_Id(boardClass.getId(), subjects.get(subjectCode).getId())) {
                        continue;
                    }
                    BoardClassSubject offering = new BoardClassSubject();
                    offering.setBoardClass(boardClass);
                    offering.setSubject(subjects.get(subjectCode));
                    boardClassSubjectRepository.save(offering);
                }
            }
        }
    }

    /** MATH/ENG/EVS everywhere; SCI/SST from ordinal ≥ 10 (Class 6 up). */
    private static List<String> offerSubjectCodes(int ordinal) {
        if (ordinal >= MIN_OFFER_ORDINAL_SCI_SST) {
            return List.of("MATH", "ENG", "EVS", "SCI", "SST");
        }
        return List.of("MATH", "ENG", "EVS");
    }

    /** India boards and the generic ALL board share the same class labels. */
    static final class India {
        static String displayName(int ordinal) {
            return switch (ordinal) {
                case 1 -> "Playgroup";
                case 2 -> "Nursery";
                case 3 -> "Junior KG";
                case 4 -> "Senior KG";
                default -> ordinal >= 5 && ordinal <= 16 ? "Class " + (ordinal - 4) : "Class " + (ordinal - 4);
            };
        }
    }

    /**
     * Board-local labels for non-India boards. ENG small classes repeat the Nursery
     * label for two ordinals (US Preschool likewise); that is why board_class does not
     * enforce a unique display name per board — identity is the (board, ordinal) pair.
     */
    static final class BoardLabels {
        static String displayName(String boardCode, int ordinal) {
            return switch (boardCode) {
                case "ENG" -> engLabel(ordinal);
                case "US" -> usLabel(ordinal);
                case "IB" -> ibLabel(ordinal);
                default -> India.displayName(ordinal);
            };
        }

        private static String engLabel(int ordinal) {
            return switch (ordinal) {
                case 2 -> "Nursery";
                case 3 -> "Nursery";
                case 4 -> "Reception";
                default -> "Year " + (ordinal - 4);
            };
        }

        private static String usLabel(int ordinal) {
            return switch (ordinal) {
                case 2, 3 -> "Preschool";
                case 4 -> "Kindergarten";
                default -> "Grade " + (ordinal - 4);
            };
        }

        private static String ibLabel(int ordinal) {
            if (ordinal <= 8) return "PYP (Class " + (ordinal - 4) + ")";
            if (ordinal <= 13) return "MYP (Class " + (ordinal - 4) + ")";
            return "DP (Class " + (ordinal - 4) + ")";
        }
    }
}