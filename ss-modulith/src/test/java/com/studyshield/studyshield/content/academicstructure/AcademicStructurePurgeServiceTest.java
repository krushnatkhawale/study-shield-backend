package com.studyshield.studyshield.content.academicstructure;

import com.studyshield.studyshield.content.academicstructure.dto.PurgeReport;
import com.studyshield.studyshield.content.entity.ContentPack;
import com.studyshield.studyshield.content.entity.ContentTier;
import com.studyshield.studyshield.content.entity.Quiz;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.ContentPackRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import com.studyshield.studyshield.content.repository.QuizBundleRepository;
import com.studyshield.studyshield.content.repository.QuizRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Purge deletes rows only (never tables), respects FK order, and refuses to cascade
 * into still-referencing content. "all" takes the content chain down first so the
 * offerings rows can go.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcademicStructurePurgeServiceTest {

    @Autowired
    private AcademicStructureLoadService loadService;
    @Autowired
    private AcademicStructurePurgeService purgeService;
    @Autowired
    private ContentPackRepository contentPackRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizBundleRepository quizBundleRepository;
    @Autowired
    private BoardClassSubjectRepository boardClassSubjectRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private BoardClassRepository boardClassRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private ClassLevelRepository classLevelRepository;

    @BeforeEach
    void startEmpty() {
        purgeService.purge("all", true);
    }

    @Test
    void purgeRequiresExplicitConfirmation() {
        assertThatThrownBy(() -> purgeService.purge("all", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirm=true");
    }

    @Test
    void purgeAllTakesTheContentChainThenTheStructureInFkOrder() {
        loadService.load(null, false, null);
        seedOneContentPackAndQuiz();

        PurgeReport report = purgeService.purge("all", true);

        assertThat(report.entities()).extracting(PurgeReport.PurgeEntityResult::table)
                .containsExactly("questions", "quizzes", "content_packs", "quiz_bundles",
                        "board_class_subject", "subjects", "board_class", "boards",
                        "countries", "class_levels");
        assertThat(quizRepository.count()).isZero();
        assertThat(contentPackRepository.count()).isZero();
        assertThat(quizBundleRepository.count()).isZero();
        assertThat(boardClassSubjectRepository.count()).isZero();
        assertThat(subjectRepository.count()).isZero();
        assertThat(boardClassRepository.count()).isZero();
        assertThat(boardRepository.count()).isZero();
        assertThat(countryRepository.count()).isZero();
        assertThat(classLevelRepository.count()).isZero();
    }

    @Test
    void purgingABoardWhoseClassesStillExistIsRefused() {
        loadService.load(null, false, null);

        assertThatThrownBy(() -> purgeService.purge("boards", true))
                .isInstanceOf(AcademicStructureConflictException.class)
                .hasMessageContaining("board_class");

        assertThat(boardRepository.count()).isEqualTo(7);
    }

    @Test
    void purgingOfferingsWhileContentPacksReferenceThemIsRefused() {
        loadService.load(null, false, null);
        seedOneContentPackAndQuiz();

        assertThatThrownBy(() -> purgeService.purge("offerings", true))
                .isInstanceOf(AcademicStructureConflictException.class)
                .hasMessageContaining("content_packs");

        assertThat(boardClassSubjectRepository.count()).isEqualTo(121);
    }

    @Test
    void purgingOneEntityDeletesOnlyItsRows() {
        loadService.load(null, false, null);

        purgeService.purge("offerings", true);

        assertThat(boardClassSubjectRepository.count()).isZero();
        assertThat(subjectRepository.count()).isEqualTo(7);
        assertThat(boardClassRepository.count()).isEqualTo(108);
        assertThat(boardRepository.count()).isEqualTo(7);
        assertThat(countryRepository.count()).isEqualTo(3);
        assertThat(classLevelRepository.count()).isEqualTo(17);
    }

    private void seedOneContentPackAndQuiz() {
        var offering = boardClassSubjectRepository.findAll().get(0);
        ContentPack pack = new ContentPack();
        pack.setName("Purge Test Pack");
        pack.setOffering(offering);
        pack.setVersion(1);
        pack.setActive(true);
        pack = contentPackRepository.save(pack);

        Quiz quiz = new Quiz();
        quiz.setContentPack(pack);
        quiz.setTitle("Purge Test Quiz");
        quiz.setQuizType(Quiz.QuizType.STANDARD);
        quiz.setContentTier(ContentTier.FREEMIUM);
        quiz.setFreemiumIndex(0);
        quizRepository.save(quiz);
    }
}
