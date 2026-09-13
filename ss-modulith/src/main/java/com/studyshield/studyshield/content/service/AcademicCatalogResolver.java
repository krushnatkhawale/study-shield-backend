package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.BoardClassSubject;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the stable academic references a request can name with display text:
 * {@code boardCode + className (+ age or subject)} → board, class level ordinal,
 * {@link BoardClass}, and {@link BoardClassSubject} offering.
 * <p>
 * Resolution rules (content is anchored to ordinals, never to display names):
 * <ul>
 *   <li>className is first matched against the board's own {@code board_class} display
 *       labels (case-insensitive), then against the generic ALL board's labels, then by
 *       heuristic (India "Class n" → ordinal n+4; ENG "Year n" → ordinal n+4; US
 *       "Grade n" → ordinal n+4; Kindergarten/Reception → 4; Nursery/Trial → 2; etc).</li>
 *   <li>offerings fall back to the generic ALL board when the requested board has none
 *       at that ordinal — same ordinal ≈ same age band, which is the only content
 *       ALL carries (board-specific syllabi are never mixed).</li>
 * </ul>
 */
@Service
public class AcademicCatalogResolver {

    private static final Logger log = LoggerFactory.getLogger(AcademicCatalogResolver.class);

    public static final String GENERIC_BOARD_CODE = "ALL";
    /** India/ENG/US numeric classes sit 5 ordinals above their literal number (Class 1 → 5). */
    private static final int CLASS_ORDINAL_OFFSET = 4;

    private final BoardRepository boardRepository;
    private final ClassLevelRepository classLevelRepository;
    private final BoardClassRepository boardClassRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;
    private final SubjectRepository subjectRepository;

    public AcademicCatalogResolver(
            BoardRepository boardRepository,
            ClassLevelRepository classLevelRepository,
            BoardClassRepository boardClassRepository,
            BoardClassSubjectRepository boardClassSubjectRepository,
            SubjectRepository subjectRepository
    ) {
        this.boardRepository = boardRepository;
        this.classLevelRepository = classLevelRepository;
        this.boardClassRepository = boardClassRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    /* ---------------------------------------------------------------- boards */

    /** Normalizes and (if needed) auto-creates a board from its code. Null/blank → ALL. */
    @Transactional
    public Board resolveOrCreateBoard(String boardCode) {
        String code = normalizeBoardCode(boardCode);
        return boardRepository.findByCode(code).orElseGet(() -> {
            log.info("[Catalog] Auto-creating board code={}", code);
            Board board = new Board();
            board.setName(GENERIC_BOARD_CODE.equals(code) ? "All Boards" : code);
            board.setCode(code);
            board.setDescription("Auto-resolved board");
            board.setMinOrdinal(1);
            board.setMaxOrdinal(16);
            board.setActive(true);
            return boardRepository.save(board);
        });
    }

    public Board getBoard(String boardCode) {
        String code = normalizeBoardCode(boardCode);
        return boardRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Board", code));
    }

    public static String normalizeBoardCode(String boardCode) {
        if (boardCode == null || boardCode.isBlank() || "all".equalsIgnoreCase(boardCode)) {
            return GENERIC_BOARD_CODE;
        }
        return boardCode.trim().toUpperCase(Locale.ROOT);
    }

    /* ------------------------------------------------------------- class levels */

    public ClassLevel classLevelForOrdinal(int ordinal) {
        return classLevelRepository.findByOrdinal(ordinal)
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel", (long) ordinal));
    }

    public boolean isWithinBoardRange(Board board, int ordinal) {
        return ordinal >= board.getMinOrdinal() && ordinal <= board.getMaxOrdinal();
    }

    /* -------------------------------------------------------------- board classes */

    /** Get-or-create the {@link BoardClass} row for (board, ordinal). */
    @Transactional
    public BoardClass resolveOrCreateBoardClass(Board board, int ordinal, String displayName) {
        return boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(board.getId(), ordinal)
                .orElseGet(() -> {
                    ClassLevel level = classLevelForOrdinal(ordinal);
                    BoardClass bc = new BoardClass();
                    bc.setBoard(board);
                    bc.setClassLevel(level);
                    bc.setDisplayName(displayName != null && !displayName.isBlank()
                            ? displayName
                            : level.getCanonicalName());
                    log.info("[Catalog] Creating board_class board={} ordinal={} display={}",
                            board.getCode(), ordinal, bc.getDisplayName());
                    return boardClassRepository.save(bc);
                });
    }

    /**
     * Resolve the board-local class label ({@link BoardClass}) for a request's className.
     * Tries the board's own labels, then the ALL board's labels, then the heuristic.
     */
    @Transactional
    public BoardClass resolveBoardClass(String boardCode, String className) {
        String code = normalizeBoardCode(boardCode);
        String name = className == null ? "" : className.trim();

        BoardClass byBoard = findBoardClassByDisplayName(code, name).orElse(null);
        if (byBoard != null) {
            return byBoard;
        }
        if (!GENERIC_BOARD_CODE.equals(code)) {
            BoardClass byAll = findBoardClassByDisplayName(GENERIC_BOARD_CODE, name).orElse(null);
            if (byAll != null) {
                return byAll;
            }
        }
        Board board = getBoard(code);
        int ordinal = ordinalForClassName(name);
        if (ordinal < 0) {
            throw new IllegalArgumentException("Cannot resolve class name: " + className);
        }
        Board boardClassOwner = isWithinBoardRange(board, ordinal) ? board : getBoard(GENERIC_BOARD_CODE);
        return resolveOrCreateBoardClass(boardClassOwner, ordinal, name);
    }

    private Optional<BoardClass> findBoardClassByDisplayName(String boardCode, String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return Optional.empty();
        }
        return boardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable(boardCode, displayName)
                .stream()
                .findFirst();
    }

    /**
     * Board-local heuristic from a className that does not match any stored label.
     * Returns -1 when the name cannot be interpreted. Numeric classes (Class n, Grade n,
     * Year n) map to {@code n + 4} — the shared India/ENG/US convention in the matrix.
     */
    public int ordinalForClassName(String className) {
        if (className == null) {
            return -1;
        }
        String t = className.trim();
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.isBlank()) return -1;
        if (containsWord(lower, "playgroup") || lower.startsWith("play group")) return 1;
        if (containsWord(lower, "nursery") || lower.equals("trial") || lower.equals("exp")
                || lower.equals("experimental") || lower.equals("promo")) return 2;
        if (containsWord(lower, "lkg") || containsWord(lower, "junior")) return 3;
        if (containsWord(lower, "ukg") || lower.contains("sr kg") || containsWord(lower, "senior")
                || containsWord(lower, "reception")) return 4;
        if (lower.startsWith("preschool")) return 2;
        if (containsWord(lower, "kindergarten")) return 4;
        if (lower.contains("year")) {
            int n = numberIn(t);
            return n > 0 ? n + CLASS_ORDINAL_OFFSET : -1;
        }
        int n = classNumber(lower);
        return n > 0 ? n + CLASS_ORDINAL_OFFSET : -1;
    }

    private static boolean containsWord(String lower, String word) {
        return lower.matches(".*\\b" + word.replace(" ", "\\s+") + "\\b.*");
    }

    private static int numberIn(String value) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})").matcher(value);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static int classNumber(String lower) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:class|grade|std|standard)?\\s*(\\d{1,2})").matcher(lower);
        return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    /* ------------------------------------------------------------------ offerings */

    /**
     * Offerings for (board, ordinal); falls back to the generic ALL board when the
     * requested board carries none at that ordinal. Never mixes board-specific syllabi.
     */
    @Transactional(readOnly = true)
    public List<BoardClassSubject> resolveOfferings(String boardCode, int ordinal) {
        String code = normalizeBoardCode(boardCode);
        List<BoardClassSubject> offerings = boardClassSubjectRepository
                .findByBoardCodeAndOrdinal(code, ordinal);
        if (offerings.isEmpty() && !GENERIC_BOARD_CODE.equals(code)) {
            offerings = boardClassSubjectRepository.findByBoardCodeAndOrdinal(GENERIC_BOARD_CODE, ordinal);
        }
        return offerings;
    }

    /** Get-or-create the offering (board_class + subject) for a request. */
    @Transactional
    public BoardClassSubject resolveOrCreateOffering(String boardCode, String className, String subjectName) {
        BoardClass boardClass = resolveBoardClass(boardCode, className);
        Subject subject = resolveSubject(subjectName);
        return resolveOrCreateOffering(boardClass, subject);
    }

    /** Get-or-create the offering for an already-resolved board class + global subject. */
    @Transactional
    public BoardClassSubject resolveOrCreateOffering(BoardClass boardClass, Subject subject) {
        return boardClassSubjectRepository
                .findByBoardClass_IdAndSubject_Id(boardClass.getId(), subject.getId())
                .orElseGet(() -> {
                    BoardClassSubject offering = new BoardClassSubject();
                    offering.setBoardClass(boardClass);
                    offering.setSubject(subject);
                    log.info("[Catalog] Creating offering board={} ordinal={} subject={}",
                            boardClass.getBoard().getCode(),
                            boardClass.getClassLevel().getOrdinal(),
                            subject.getCode());
                    return boardClassSubjectRepository.save(offering);
                });
    }

    /** Global subject lookup by code/name; auto-creates (with a code derived from the name) when missing. */
    @Transactional
    public Subject resolveSubject(String subjectName) {
        if (subjectName == null || subjectName.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        String code = subjectName.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
        return subjectRepository.findByCodeIgnoreCase(code)
                .or(() -> subjectRepository.findByCode(code))
                .orElseGet(() -> {
                    Subject subject = new Subject();
                    subject.setName(subjectName.trim());
                    subject.setCode(code);
                    subject.setActive(true);
                    return subjectRepository.save(subject);
                });
    }
}