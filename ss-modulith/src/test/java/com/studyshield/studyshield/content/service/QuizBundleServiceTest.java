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
 * top-level business rule: every class whose subjects carry active content must
 * return quizzes to the mobile app.
 */
class QuizBundleServiceTest {

    private QuizBundleRepository quizBundleRepository;
    private QuizBundleSeeder catalogSeeder;
    private SubjectRepository subjectRepository;
    private ContentPackRepository contentPackRepository;
    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;
    private QuestionService questionService;
    private QuizBundleService service;

    private static final com.studyshield.studyshield.content.entity.ClassGrade CLASS_GRADE = classGrade(1L);
    private static final Subject MATH = subject(10L, "Math", CLASS_GRADE);
    private static final Subject EVS  = subject(11L, "EVS",  CLASS_GRADE);

    @BeforeEach
    void setUp() {
        quizBundleRepository = mock(QuizBundleRepository.class);
        subjectRepository = mock(SubjectRepository.class);
        contentPackRepository = mock(ContentPackRepository.class);
        quizRepository = mock(QuizRepository.class);
        questionRepository = mock(QuestionRepository.class);
        // Map-to-response run on a real service (Mockito cannot instrument the concrete @Service
        // class on the JDK default); its repos are never touched during bundle mapping.
        questionService = new QuestionService(questionRepository, quizRepository);
        // Real subclass (Mockito's inline mock maker cannot instrument the concrete @Service
        // class on the JDK default — see QuestionFeedbackControllerTest). ensureCatalogForClass
        // resolves only to the pre-built class grade, so the request path isolates bundle logic.
        catalogSeeder = new QuizBundleSeeder(
                mock(BoardRepository.class), mock(ClassGradeRepository.class),
                subjectRepository, contentPackRepository, quizRepository, questionRepository) {
            @Override
            public com.studyshield.studyshield.content.entity.ClassGrade ensureCatalogForClass(
                    String className, String boardCode) {
                return CLASS_GRADE;
            }
        };
        service = new QuizBundleService(quizBundleRepository, catalogSeeder, subjectRepository,
                contentPackRepository, quizRepository, questionRepository, questionService);

        // No existing bundle for this holder
        when(quizBundleRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(quizBundleRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bankLoadedPackIsServedWhenNoFreemiumPackExistsForASubject() {
        when(subjectRepository.findByClassGradeIdOrderByDisplayOrderAscIdAsc(CLASS_GRADE.getId())).thenReturn(List.of(MATH));

        ContentPack loadedPack = contentPack("Loaded Math", 100L);
        when(contentPackRepository.findBySubjectId(MATH.getId())).thenReturn(List.of(loadedPack));

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
    void multipleSubjectsWithOnlyBankLoadedPacksAllAppearInTheBundle() {
        when(subjectRepository.findByClassGradeIdOrderByDisplayOrderAscIdAsc(CLASS_GRADE.getId())).thenReturn(List.of(MATH, EVS));

        ContentPack mathPack = contentPack("Loaded Math", 100L);
        ContentPack evsPack  = contentPack("Loaded EVS",  101L);
        when(contentPackRepository.findBySubjectId(MATH.getId())).thenReturn(List.of(mathPack));
        when(contentPackRepository.findBySubjectId(EVS.getId())).thenReturn(List.of(evsPack));

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
    void anErrorIsThrownWhenASubjectHasNoActivePackAtAll() {
        when(subjectRepository.findByClassGradeIdOrderByDisplayOrderAscIdAsc(CLASS_GRADE.getId())).thenReturn(List.of(MATH));
        when(contentPackRepository.findBySubjectId(MATH.getId())).thenReturn(Collections.emptyList());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        service.issue(new QuizBundleRequest(
                                "Class 7", null, null, null, null, "dev-1", null, false)))
                .isInstanceOf(com.studyshield.studyshield.common.exception.ResourceNotFoundException.class);
    }

    @Test
    void bundlePicksSubjectsInDisplayOrderNotInsertionOrder() {
        // EVS appears first in the repository list but carries a later displayOrder,
        // so the bundle must pick Math before EVS (first QUIZZES_PER_CLASS subjects win).
        Subject math = subject(10L, "Math", CLASS_GRADE);
        math.setDisplayOrder(1);
        Subject evs = subject(11L, "EVS", CLASS_GRADE);
        evs.setDisplayOrder(2);

        when(subjectRepository.findByClassGradeIdOrderByDisplayOrderAscIdAsc(CLASS_GRADE.getId()))
                .thenReturn(List.of(math, evs));

        ContentPack mathPack = contentPack("Loaded Math", 100L);
        ContentPack evsPack  = contentPack("Loaded EVS",  101L);
        when(contentPackRepository.findBySubjectId(math.getId())).thenReturn(List.of(mathPack));
        when(contentPackRepository.findBySubjectId(evs.getId())).thenReturn(List.of(evsPack));

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

    private static com.studyshield.studyshield.content.entity.ClassGrade classGrade(Long id) {
        com.studyshield.studyshield.content.entity.ClassGrade cg = new com.studyshield.studyshield.content.entity.ClassGrade();
        cg.setId(id);
        cg.setName("Class 7");
        return cg;
    }

    private static Subject subject(Long id, String name,
                                   com.studyshield.studyshield.content.entity.ClassGrade cg) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        s.setActive(true);
        s.setClassGrade(cg);
        return s;
    }
}
