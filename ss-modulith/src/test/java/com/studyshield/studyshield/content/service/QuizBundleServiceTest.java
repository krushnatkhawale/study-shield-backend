package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuizBundleRequest;
import com.studyshield.studyshield.content.dto.QuizBundleResponse;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.content.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies that bank-loaded packs are included in the bundle for a class — the
 * top-level business rule: every class whose offerings carry active content must
 * return quizzes to the mobile app. Content is anchored to offerings
 * (board + class ordinal + global subject), never to display names.
 */
class QuizBundleServiceTest {

    private QuizBundleRepository quizBundleRepository;
    private QuizBundleSeeder catalogSeeder;
    private AcademicCatalogResolver catalogResolver;
    private BoardRepository boardRepository;
    private ClassLevelRepository classLevelRepository;
    private BoardClassRepository boardClassRepository;
    private BoardClassSubjectRepository boardClassSubjectRepository;
    private SubjectRepository subjectRepository;
    private ContentPackRepository contentPackRepository;
    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;
    private QuestionService questionService;
    private QuizBundleService service;

    private static final BoardClass BOARD_CLASS = boardClass(5L, 7);
    private static final BoardClassSubject MATH_OFFERING = offering(900L, "Math");
    private static final BoardClassSubject EVS_OFFERING = offering(901L, "EVS");

    @BeforeEach
    void setUp() {
        quizBundleRepository = mock(QuizBundleRepository.class);
        contentPackRepository = mock(ContentPackRepository.class);
        quizRepository = mock(QuizRepository.class);
        questionRepository = mock(QuestionRepository.class);
        // Map-to-response runs on a real service (Mockito cannot instrument concrete
        // @Service classes on JDK 26); its repos are never touched during bundle mapping.
        questionService = new QuestionService(questionRepository, quizRepository);
        // Real subclass (Mockito's inline mock maker cannot instrument the concrete @Service
        // class on the JDK default). ensureCatalogForClass resolves only to the pre-built
        // board class, so the request path isolates bundle logic.
        boardRepository = mock(BoardRepository.class);
        classLevelRepository = mock(ClassLevelRepository.class);
        boardClassRepository = mock(BoardClassRepository.class);
        boardClassSubjectRepository = mock(BoardClassSubjectRepository.class);
        subjectRepository = mock(SubjectRepository.class);
        catalogResolver = new AcademicCatalogResolver(
                boardRepository, classLevelRepository, boardClassRepository,
                boardClassSubjectRepository, subjectRepository);
        catalogSeeder = new QuizBundleSeeder(
                catalogResolver, contentPackRepository, quizRepository, questionRepository) {
            @Override
            public BoardClass ensureCatalogForClass(String className, String boardCode) {
                return BOARD_CLASS;
            }
        };
        service = new QuizBundleService(quizBundleRepository, catalogSeeder, catalogResolver,
                classLevelRepository, contentPackRepository, quizRepository, questionRepository,
                questionService);

        // No existing bundle for this holder; offerings resolve to the two preset ones.
        when(quizBundleRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(quizBundleRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7))
                .thenReturn(List.of(MATH_OFFERING, EVS_OFFERING));
    }

    @Test
    void bankLoadedPackIsServedWhenNoFreemiumPackExistsForAnOffering() {
        when(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7))
                .thenReturn(List.of(MATH_OFFERING));

        ContentPack loadedPack = contentPack("Loaded Math", 100L);
        when(contentPackRepository.findByOfferingId(MATH_OFFERING.getId())).thenReturn(List.of(loadedPack));

        Quiz mathQuiz = quiz("Math · Quiz 1", 200L, loadedPack);
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                100L, ContentTier.FREEMIUM)).thenReturn(List.of(mathQuiz));

        when(questionRepository.findByQuizIdAndBlacklistedFalse(200L))
                .thenReturn(List.of(question(), question(), question()));
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mathQuiz));

        QuizBundleResponse response = service.issue(new QuizBundleRequest(
                "Class 7", null, null, null, null, "dev-1", null, false));

        assertThat(response.quizCount()).isGreaterThanOrEqualTo(1);
        assertThat(response.quizzes()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.quizzes().getFirst().title()).isEqualTo("Math · Quiz 1");
        assertThat(response.quizzes().getFirst().contentPackName()).isEqualTo("Loaded Math");
    }

    @Test
    void multipleOfferingsWithOnlyBankLoadedPacksAllAppearInTheBundle() {
        ContentPack mathPack = contentPack("Loaded Math", 100L);
        ContentPack evsPack  = contentPack("Loaded EVS",  101L);
        when(contentPackRepository.findByOfferingId(MATH_OFFERING.getId())).thenReturn(List.of(mathPack));
        when(contentPackRepository.findByOfferingId(EVS_OFFERING.getId())).thenReturn(List.of(evsPack));

        Quiz mathQuiz = quiz("Math · Quiz 1", 200L, mathPack);
        Quiz evsQuiz  = quiz("EVS · Quiz 1",  201L, evsPack);
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                100L, ContentTier.FREEMIUM)).thenReturn(List.of(mathQuiz));
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                101L, ContentTier.FREEMIUM)).thenReturn(List.of(evsQuiz));

        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(
                List.of(question(), question(), question()));
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mathQuiz));
        when(quizRepository.findById(201L)).thenReturn(Optional.of(evsQuiz));

        QuizBundleResponse response = service.issue(new QuizBundleRequest(
                "Class 7", null, null, null, null, "dev-1", null, false));

        assertThat(response.quizCount()).isEqualTo(2);
        assertThat(response.subjects()).containsExactly("Math", "EVS");
    }

    @Test
    void anErrorIsThrownWhenAnOfferingHasNoActivePackAtAll() {
        when(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7))
                .thenReturn(List.of(MATH_OFFERING));
        when(contentPackRepository.findByOfferingId(MATH_OFFERING.getId())).thenReturn(Collections.emptyList());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        service.issue(new QuizBundleRequest(
                                "Class 7", null, null, null, null, "dev-1", null, false)))
                .isInstanceOf(com.studyshield.studyshield.common.exception.InsufficientStockException.class);
    }

    @Test
    void bundlePicksOfferingsInSubjectDisplayOrderNotInsertionOrder() {
        Subject math = subject(10L, "Math");
        math.setDisplayOrder(1);
        Subject evs = subject(11L, "EVS");
        evs.setDisplayOrder(2);
        BoardClassSubject mathOffering = offering(900L, math);
        BoardClassSubject evsOffering = offering(901L, evs);
        when(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7))
                .thenReturn(List.of(mathOffering, evsOffering));

        ContentPack mathPack = contentPack("Loaded Math", 100L);
        ContentPack evsPack  = contentPack("Loaded EVS",  101L);
        when(contentPackRepository.findByOfferingId(900L)).thenReturn(List.of(mathPack));
        when(contentPackRepository.findByOfferingId(901L)).thenReturn(List.of(evsPack));

        Quiz mathQuiz = quiz("Math · Quiz 1", 200L, mathPack);
        Quiz evsQuiz  = quiz("EVS · Quiz 1",  201L, evsPack);
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                100L, ContentTier.FREEMIUM)).thenReturn(List.of(mathQuiz));
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                101L, ContentTier.FREEMIUM)).thenReturn(List.of(evsQuiz));

        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(
                List.of(question(), question(), question()));
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mathQuiz));
        when(quizRepository.findById(201L)).thenReturn(Optional.of(evsQuiz));

        QuizBundleResponse response = service.issue(new QuizBundleRequest(
                "Class 7", null, null, null, null, "dev-1", null, false));

        assertThat(response.subjects()).containsExactly("Math", "EVS");
    }

    @Test
    void allFourOfferingsWithQuestionsAppearInTheBundle() {
        BoardClassSubject engOffering = offering(902L, "English");
        BoardClassSubject hinOffering = offering(903L, "Hindi");
        when(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7))
                .thenReturn(List.of(MATH_OFFERING, EVS_OFFERING, engOffering, hinOffering));

        ContentPack mathPack = contentPack("Freemium Math", 100L);
        ContentPack evsPack = contentPack("Freemium EVS", 101L);
        ContentPack engPack = contentPack("Freemium English", 102L);
        ContentPack hinPack = contentPack("Freemium Hindi", 103L);
        when(contentPackRepository.findByOfferingId(900L)).thenReturn(List.of(mathPack));
        when(contentPackRepository.findByOfferingId(901L)).thenReturn(List.of(evsPack));
        when(contentPackRepository.findByOfferingId(902L)).thenReturn(List.of(engPack));
        when(contentPackRepository.findByOfferingId(903L)).thenReturn(List.of(hinPack));

        Quiz mathQuiz = quiz("Math · Quiz 1", 200L, mathPack);
        Quiz evsQuiz = quiz("EVS · Quiz 1", 201L, evsPack);
        Quiz engQuiz = quiz("English · Quiz 1", 202L, engPack);
        Quiz hinQuiz = quiz("Hindi · Quiz 1", 203L, hinPack);
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                100L, ContentTier.FREEMIUM)).thenReturn(List.of(mathQuiz));
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                101L, ContentTier.FREEMIUM)).thenReturn(List.of(evsQuiz));
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                102L, ContentTier.FREEMIUM)).thenReturn(List.of(engQuiz));
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                103L, ContentTier.FREEMIUM)).thenReturn(List.of(hinQuiz));

        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(
                List.of(question(), question(), question()));
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mathQuiz));
        when(quizRepository.findById(201L)).thenReturn(Optional.of(evsQuiz));
        when(quizRepository.findById(202L)).thenReturn(Optional.of(engQuiz));
        when(quizRepository.findById(203L)).thenReturn(Optional.of(hinQuiz));

        QuizBundleResponse response = service.issue(new QuizBundleRequest(
                "Class 3", null, null, null, null, "dev-1", null, false));

        assertThat(response.quizCount()).isEqualTo(4);
        assertThat(response.subjects()).containsExactly("Math", "EVS", "English", "Hindi");
    }

    @Test
    void idempotentBundleKeyIsBuiltFromOfferingIdsNotClassNames() {
        ContentPack mathPack = contentPack("Freemium Math", 100L);
        when(contentPackRepository.findByOfferingId(MATH_OFFERING.getId())).thenReturn(List.of(mathPack));
        Quiz mathQuiz = quiz("Math · Quiz 1", 200L, mathPack);
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                100L, ContentTier.FREEMIUM)).thenReturn(List.of(mathQuiz));
        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(
                List.of(question(), question(), question()));
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mathQuiz));

        service.issue(new QuizBundleRequest(
                "Class 7", null, null, null, null, "dev-1", null, false));

        var captor = org.mockito.ArgumentCaptor.forClass(QuizBundle.class);
        verify(quizBundleRepository).saveAndFlush(captor.capture());
        QuizBundle saved = captor.getValue();
        assertThat(saved.getIdempotencyKey())
                .startsWith("offering:")
                .contains("900")
                .contains("901")
                .doesNotContain("Class 7")
                .doesNotContain("class");
        assertThat(saved.getOffering()).isEqualTo(MATH_OFFERING);
        assertThat(saved.getOfferingIds()).containsExactly(900L, 901L);
    }

    private static Quiz quiz(String title, Long id, ContentPack pack) {
        Quiz q = new Quiz();
        q.setId(id);
        q.setTitle(title);
        q.setContentPack(pack);
        q.setFreemiumIndex(1);
        q.setActive(true);
        q.setContentTier(ContentTier.FREEMIUM);
        return q;
    }

    private static ContentPack contentPack(String name, Long id) {
        ContentPack p = new ContentPack();
        p.setId(id);
        p.setName(name);
        p.setActive(true);
        return p;
    }

    private static com.studyshield.studyshield.content.entity.Question question() {
        com.studyshield.studyshield.content.entity.Question q =
                new com.studyshield.studyshield.content.entity.Question();
        q.setId(1L);
        q.setQuestionText("Q");
        Quiz qz = new Quiz();
        qz.setId(200L);
        q.setQuiz(qz);
        return q;
    }

    private static BoardClass boardClass(Long id, int ordinal) {
        BoardClass bc = new BoardClass();
        bc.setId(id);
        Board board = new Board();
        board.setId(1L);
        board.setCode("ALL");
        bc.setBoard(board);
        ClassLevel level = new ClassLevel();
        level.setId((long) ordinal);
        level.setOrdinal(ordinal);
        level.setCanonicalName("Class " + (ordinal - 4));
        bc.setClassLevel(level);
        bc.setDisplayName("Class " + (ordinal - 4));
        return bc;
    }

    private static BoardClassSubject offering(Long id, String subjectName) {
        return offering(id, subject(10L, subjectName));
    }

    private static BoardClassSubject offering(Long id, Subject subject) {
        BoardClassSubject o = new BoardClassSubject();
        o.setId(id);
        o.setBoardClass(BOARD_CLASS);
        o.setSubject(subject);
        return o;
    }

    private static Subject subject(Long id, String name) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        s.setActive(true);
        return s;
    }
}