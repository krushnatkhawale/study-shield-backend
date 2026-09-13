package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuestionBankLoadItem;
import com.studyshield.studyshield.content.dto.QuestionBankLoadResponse;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.content.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Bank-loaded quizzes must land in a pack the mobile bundle can serve. The loader now
 * anchors content to an offering (board + class ordinal + global subject) and names
 * packs {@code Freemium <Subject>} (the convention the bundle service and the startup
 * seeder share), so newly loaded questions are never invisible to the app.
 */
class QuestionBankLoaderTest {

    private BoardRepository boardRepository;
    private ClassLevelRepository classLevelRepository;
    private BoardClassRepository boardClassRepository;
    private BoardClassSubjectRepository boardClassSubjectRepository;
    private SubjectRepository subjectRepository;
    private ContentPackRepository contentPackRepository;
    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;
    private QuestionBankLoader loader;

    private static final Board BOARD = board();
    private static final ClassLevel CLASS_7 = classLevel();
    private static final Subject MATH = math();

    @BeforeEach
    void setUp() {
        // Concrete @Service classes cannot be mocked on JDK 26 (Mockito inline maker), so the
        // resolver runs as a real instance backed by mocked repositories.
        boardRepository = mock(BoardRepository.class);
        classLevelRepository = mock(ClassLevelRepository.class);
        boardClassRepository = mock(BoardClassRepository.class);
        boardClassSubjectRepository = mock(BoardClassSubjectRepository.class);
        subjectRepository = mock(SubjectRepository.class);
        contentPackRepository = mock(ContentPackRepository.class);
        quizRepository = mock(QuizRepository.class);
        questionRepository = mock(QuestionRepository.class);
        AcademicCatalogResolver catalogResolver = new AcademicCatalogResolver(
                boardRepository, classLevelRepository, boardClassRepository,
                boardClassSubjectRepository, subjectRepository);
        loader = new QuestionBankLoader(catalogResolver, boardRepository, boardClassRepository,
                boardClassSubjectRepository, subjectRepository, contentPackRepository,
                quizRepository, questionRepository);

        // Board exists; class label and offering/subject lookups are empty (creation branch).
        // Existence probes read as "absent" so the created counters fire on first load.
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.of(BOARD));
        when(boardRepository.existsByCode(any())).thenReturn(false);
        when(boardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable(any(), any()))
                .thenReturn(Collections.emptyList());
        when(classLevelRepository.findByOrdinal(11)).thenReturn(Optional.of(CLASS_7));
        when(boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(any(), anyInt()))
                .thenReturn(Optional.empty());
        when(boardClassRepository.existsByBoard_CodeAndClassLevel_Ordinal(any(), anyInt()))
                .thenReturn(false);
        when(subjectRepository.findByCodeIgnoreCase("MATH")).thenReturn(Optional.of(MATH));
        when(subjectRepository.findByCode("MATH")).thenReturn(Optional.of(MATH));
        when(subjectRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
        when(boardClassSubjectRepository.findByBoardClass_IdAndSubject_Id(any(), any()))
                .thenReturn(Optional.empty());
        when(boardClassSubjectRepository.existsByBoardClass_IdAndSubject_Id(any(), any()))
                .thenReturn(false);
        when(contentPackRepository.findByOfferingId(any())).thenReturn(Collections.emptyList());

        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                any(), eq(ContentTier.FREEMIUM))).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizId(any())).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(Collections.emptyList());

        // Persistence: save() echoes, and quizzes get an id so questions can attach
        when(boardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(classLevelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(boardClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(boardClassSubjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentPackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizRepository.save(any())).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            if (q.getId() == null) {
                q.setId(500L);
            }
            return q;
        });
        when(questionRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void loadedQuestionsArePlacedInAFreemiumNamedPack() {
        QuestionBankLoadResponse response = loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        "What is 12 × 3?", QuestionType.SINGLE_CHOICE,
                        List.of("36", "32", "24", "30"), "36", null, 0)));

        ArgumentCaptor<ContentPack> captor = ArgumentCaptor.forClass(ContentPack.class);
        verify(contentPackRepository).save(captor.capture());
        ContentPack savedPack = captor.getValue();

        assertThat(savedPack.getName()).isEqualTo("Freemium Math");
        assertThat(savedPack.getDescription()).isEqualTo("Freemium catalog pack");
        assertThat(savedPack.getOffering()).isNotNull();
        assertThat(savedPack.getOffering().getBoardClass().getClassLevel().getOrdinal()).isEqualTo(11);
        assertThat(savedPack.getOffering().getSubject()).isEqualTo(MATH);
        assertThat(response.contentPacksCreated()).isEqualTo(1);
        assertThat(response.questionsCreated()).isEqualTo(1);
    }

    @Test
    void aPreviouslyLoadedPackIsConvertedToFreemiumNaming() {
        ContentPack existingLoadedPack = pack("Loaded Math", 99L);
        when(contentPackRepository.findByOfferingId(any())).thenReturn(List.of(existingLoadedPack));

        loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        "What is 5 + 7?", QuestionType.SINGLE_CHOICE,
                        List.of("12", "11", "10", "13"), "12", null, 0)));

        // The existing pack was renamed, not replaced with a brand-new one
        assertThat(existingLoadedPack.getName()).isEqualTo("Freemium Math");
        assertThat(existingLoadedPack.getId()).isEqualTo(99L);
        assertThat(existingLoadedPack.getOffering()).isNotNull();
        // save was called for the renamed pack; no fresh pack was created
        verify(contentPackRepository, atLeastOnce()).save(existingLoadedPack);
    }

    @Test
    void anExistingFreemiumPackIsReusedForNewLoads() {
        ContentPack existingFreemiumPack = pack("Freemium Math", 88L);
        when(contentPackRepository.findByOfferingId(any())).thenReturn(List.of(existingFreemiumPack));

        QuestionBankLoadResponse response = loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        "What is 9 × 9?", QuestionType.SINGLE_CHOICE,
                        List.of("81", "72", "84", "90"), "81", null, 0)));

        // The loader should NOT create or rename a pack — reuse the existing freemium pack
        assertThat(response.contentPacksCreated()).isEqualTo(0);
        verify(contentPackRepository, never()).save(any(ContentPack.class));
        // Question still loaded into the quiz under the existing pack
        assertThat(response.questionsCreated()).isEqualTo(1);
    }

    @Test
    void duplicateQuestionTextWithinAQuizIsSkipped() {
        String text = "What is 2 + 2?";
        when(questionRepository.findByQuizId(500L)).thenReturn(
                List.of(existingQuestion("What is a prime number?", 501L)));

        QuestionBankLoadResponse response = loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        text, QuestionType.SINGLE_CHOICE,
                        List.of("4", "3", "5", "2"), "4", null, 0),
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        text, QuestionType.SINGLE_CHOICE,
                        List.of("4", "3", "5", "2"), "4", null, 0)));

        assertThat(response.questionsCreated()).isEqualTo(1);
        assertThat(response.questionsSkipped()).isEqualTo(1);
    }

    private static ContentPack pack(String name, Long id) {
        ContentPack p = new ContentPack();
        p.setName(name);
        p.setActive(true);
        p.setId(id);
        return p;
    }

    private static Question existingQuestion(String text, Long id) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText(text);
        Quiz qz = new Quiz();
        qz.setId(500L);
        q.setQuiz(qz);
        return q;
    }

    private static Board board() {
        Board b = new Board();
        b.setId(1L);
        b.setCode("ALL");
        b.setName("All Boards");
        return b;
    }

    private static ClassLevel classLevel() {
        ClassLevel level = new ClassLevel();
        level.setId(11L);
        level.setOrdinal(11);
        level.setCanonicalName("Class 7");
        return level;
    }

    private static Subject math() {
        Subject s = new Subject();
        s.setId(10L);
        s.setName("Math");
        s.setCode("MATH");
        s.setActive(true);
        return s;
    }
}