package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.controller.BoardClassController;
import com.studyshield.studyshield.content.controller.ContentPackController;
import com.studyshield.studyshield.content.controller.OfferingController;
import com.studyshield.studyshield.content.controller.QuizController;
import com.studyshield.studyshield.content.dto.BoardClassRequest;
import com.studyshield.studyshield.content.dto.ClassLevelRequest;
import com.studyshield.studyshield.content.dto.ContentPackRequest;
import com.studyshield.studyshield.content.dto.OfferingRequest;
import com.studyshield.studyshield.content.dto.QuizRequest;
import com.studyshield.studyshield.content.dto.SubjectRequest;
import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.ContentPack;
import com.studyshield.studyshield.content.entity.ContentTier;
import com.studyshield.studyshield.content.entity.Offering;
import com.studyshield.studyshield.content.entity.Quiz;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassGradeRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.ContentPackRepository;
import com.studyshield.studyshield.content.repository.OfferingRepository;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.content.repository.QuizRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Academic-structure + offering association coverage: class-levels CRUD,
 * board-classes (+GET /board/{boardId}), offerings (+GET /board-class/{boardClassId}),
 * content-packs by offering, quizzes by offering, global subject (null classGrade).
 */
class ContentOfferingAssociationTest {

    @Test
    void classLevelCrudRoundTrip() {
        ClassLevelRepository repo = mock(ClassLevelRepository.class);
        ClassLevelService service = new ClassLevelService(repo);
        ClassLevel entity = new ClassLevel();
        entity.setId(1L);
        entity.setOrdinal(3);
        entity.setSlug("class-3");
        entity.setCanonicalName("Class 3");
        when(repo.save(any())).thenReturn(entity);
        when(repo.findById(1L)).thenReturn(Optional.of(entity));

        var created = service.create(new ClassLevelRequest(3, "class-3", "Class 3", 7.0, 8.0, "PRIMARY", null));
        assertThat(created.slug()).isEqualTo("class-3");
        assertThat(service.getById(1L).canonicalName()).isEqualTo("Class 3");
        service.delete(1L);
    }

    @Test
    void boardClassesListByBoard() {
        BoardRepository boardRepo = mock(BoardRepository.class);
        ClassLevelRepository levelRepo = mock(ClassLevelRepository.class);
        BoardClassRepository repo = mock(BoardClassRepository.class);
        BoardClassService service = new BoardClassService(repo, boardRepo, levelRepo);

        Board board = new Board();
        board.setId(2L);
        ClassLevel level = new ClassLevel();
        level.setId(3L);
        BoardClass bc = new BoardClass();
        bc.setId(9L);
        bc.setBoard(board);
        bc.setClassLevel(level);
        bc.setDisplayName("CBSE Class 3");
        when(boardRepo.findById(2L)).thenReturn(Optional.of(board));
        when(levelRepo.findById(3L)).thenReturn(Optional.of(level));
        when(repo.save(any())).thenReturn(bc);
        when(repo.findByBoardId(2L)).thenReturn(List.of(bc));

        var created = service.create(new BoardClassRequest(2L, 3L, "CBSE Class 3"));
        assertThat(created.displayName()).isEqualTo("CBSE Class 3");
        assertThat(service.getByBoardId(2L)).hasSize(1);

        BoardClassController controller = new BoardClassController(service);
        ResponseEntity<?> viaHttp = controller.getByBoardId(2L);
        assertThat(viaHttp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((List<?>) viaHttp.getBody()).hasSize(1);
    }

    @Test
    void offeringsListByBoardClass() {
        BoardClassRepository bcRepo = mock(BoardClassRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);
        OfferingRepository repo = mock(OfferingRepository.class);
        OfferingService service = new OfferingService(repo, bcRepo, subjectRepo);

        BoardClass bc = new BoardClass();
        bc.setId(9L);
        Subject subject = Subject.builder().name("Maths").code("MATH").build();
        subject.setId(5L);
        Offering offering = new Offering();
        offering.setId(11L);
        offering.setBoardClass(bc);
        offering.setSubject(subject);
        when(bcRepo.findById(9L)).thenReturn(Optional.of(bc));
        when(subjectRepo.findById(5L)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenReturn(offering);
        when(repo.findByBoardClassId(9L)).thenReturn(List.of(offering));

        var created = service.create(new OfferingRequest(9L, 5L));
        assertThat(created.boardClassId()).isEqualTo(9L);
        assertThat(service.getByBoardClassId(9L)).hasSize(1);

        OfferingController controller = new OfferingController(service);
        assertThat(((List<?>) controller.getByBoardClassId(9L).getBody())).hasSize(1);
    }

    @Test
    void contentPacksByOfferingPersistOfferingId() {
        ContentPackRepository packRepo = mock(ContentPackRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);
        OfferingRepository offeringRepo = mock(OfferingRepository.class);
        ContentPackService service = new ContentPackService(packRepo, subjectRepo, offeringRepo);

        Subject subject = Subject.builder().name("Maths").code("MATH").build();
        subject.setId(5L);
        Offering offering = new Offering();
        offering.setId(11L);
        ContentPack pack = ContentPack.builder().name("Pack").subject(subject).offering(offering).build();
        pack.setId(21L);
        when(subjectRepo.findById(5L)).thenReturn(Optional.of(subject));
        when(offeringRepo.findById(11L)).thenReturn(Optional.of(offering));
        when(packRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(packRepo.findByOfferingId(11L)).thenReturn(List.of(pack));

        var created = service.create(new ContentPackRequest("Pack", null, 5L, 11L, 1, true,
                ContentTier.FREEMIUM, null, null));
        assertThat(created.offeringId()).isEqualTo(11L);
        assertThat(service.getByOfferingId(11L)).hasSize(1);
        assertThat(service.getByOfferingId(11L).getFirst().offeringId()).isEqualTo(11L);

        ContentPackController controller = new ContentPackController(service);
        assertThat(((List<?>) controller.getByOfferingId(11L).getBody())).hasSize(1);

        // legacy path still works: null offering
        var legacy = service.create(new ContentPackRequest("Legacy", null, 5L, null, 1, true,
                ContentTier.FREEMIUM, null, null));
        assertThat(legacy.offeringId()).isNull();
    }

    @Test
    void quizzesByOfferingPersistOfferingId() {
        QuizRepository quizRepo = mock(QuizRepository.class);
        ContentPackRepository packRepo = mock(ContentPackRepository.class);
        OfferingRepository offeringRepo = mock(OfferingRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);
        QuestionRepository questionRepo = mock(QuestionRepository.class);
        QuestionService questionService = new QuestionService(questionRepo, quizRepo);
        QuizService service = new QuizService(quizRepo, packRepo, offeringRepo, subjectRepo,
                questionRepo, questionService);

        Subject subject = Subject.builder().name("Maths").code("MATH").build();
        subject.setId(5L);
        ContentPack pack = ContentPack.builder().name("Pack").subject(subject).build();
        pack.setId(21L);
        Offering offering = new Offering();
        offering.setId(11L);
        Quiz quiz = Quiz.builder().title("Q1").contentPack(pack).offering(offering).build();
        quiz.setId(31L);
        when(packRepo.findById(21L)).thenReturn(Optional.of(pack));
        when(offeringRepo.findById(11L)).thenReturn(Optional.of(offering));
        when(quizRepo.save(any())).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            if (q.getId() == null) {
                q.setId(31L);
            }
            return q;
        });
        when(quizRepo.findByOfferingId(11L)).thenReturn(List.of(quiz));

        var created = service.create(new QuizRequest("Q1", null, 21L, 11L, null, null, null, null, null, true));
        assertThat(created.offeringId()).isEqualTo(11L);
        assertThat(service.getByOfferingId(11L)).hasSize(1);

        QuizController controller = new QuizController(service);
        assertThat(((List<?>) controller.getByOfferingId(11L).getBody())).hasSize(1);
    }

    @Test
    void globalSubjectWithNullClassGrade() {
        SubjectRepository subjectRepo = mock(SubjectRepository.class);
        ClassGradeRepository gradeRepo = mock(ClassGradeRepository.class);
        SubjectService service = new SubjectService(subjectRepo, gradeRepo);
        Subject global = Subject.builder().name("General Knowledge").code("GK").classGrade(null).build();
        global.setId(77L);
        when(subjectRepo.save(any())).thenReturn(global);

        var created = service.create(new SubjectRequest("General Knowledge", "GK", null, null, true, 0));
        assertThat(created.name()).isEqualTo("General Knowledge");
    }
}
