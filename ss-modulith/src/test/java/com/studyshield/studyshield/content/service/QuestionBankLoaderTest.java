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
 * names packs {@code Freemium <Subject>} (the convention the bundle service and the
 * startup seeder share) so newly loaded questions are never invisible to the app.
 */
class QuestionBankLoaderTest {

    private BoardRepository boardRepository;
    private ClassGradeRepository classGradeRepository;
    private SubjectRepository subjectRepository;
    private ContentPackRepository contentPackRepository;
    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;
    private QuestionBankLoader loader;

    @BeforeEach
    void setUp() {
        boardRepository = mock(BoardRepository.class);
        classGradeRepository = mock(ClassGradeRepository.class);
        subjectRepository = mock(SubjectRepository.class);
        contentPackRepository = mock(ContentPackRepository.class);
        quizRepository = mock(QuizRepository.class);
        questionRepository = mock(QuestionRepository.class);
        loader = new QuestionBankLoader(boardRepository, classGradeRepository, subjectRepository,
                contentPackRepository, quizRepository, questionRepository);

        // save() returns the entity as-is so entity.getId() determines counters
        when(boardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(classGradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentPackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void loadedQuestionsArePlacedInAFreemiumNamedPack() {
        // Board / ClassGrade / Subject chains — fresh DB (nothing found)
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.empty());
        when(classGradeRepository.findFirstByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(subjectRepository.findByClassGradeIdAndNameIgnoreCase(any(), any())).thenReturn(Optional.empty());
        when(contentPackRepository.findBySubjectId(any())).thenReturn(Collections.emptyList());
        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                any(), eq(ContentTier.FREEMIUM))).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizId(any())).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(Collections.emptyList());

        QuestionBankLoadResponse response = loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        "What is 12 × 3?", QuestionType.SINGLE_CHOICE,
                        List.of("36", "32", "24", "30"), "36", null, 0)));

        ArgumentCaptor<ContentPack> captor = ArgumentCaptor.forClass(ContentPack.class);
        verify(contentPackRepository).save(captor.capture());
        ContentPack savedPack = captor.getValue();

        assertThat(savedPack.getName()).isEqualTo("Freemium Math");
        assertThat(savedPack.getDescription()).isEqualTo("Freemium catalog pack");
        assertThat(response.contentPacksCreated()).isEqualTo(1);
        assertThat(response.questionsCreated()).isEqualTo(1);
    }

    @Test
    void aPreviouslyLoadedPackIsConvertedToFreemiumNaming() {
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.empty());
        when(classGradeRepository.findFirstByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(subjectRepository.findByClassGradeIdAndNameIgnoreCase(any(), any())).thenReturn(Optional.empty());

        ContentPack existingLoadedPack = pack("Loaded Math", 99L);
        when(contentPackRepository.findBySubjectId(any())).thenReturn(List.of(existingLoadedPack));

        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                any(), eq(ContentTier.FREEMIUM))).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizId(any())).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(Collections.emptyList());

        loader.load(List.of(
                new QuestionBankLoadItem("ALL", "Class 7", null, "Math",
                        "What is 5 + 7?", QuestionType.SINGLE_CHOICE,
                        List.of("12", "11", "10", "13"), "12", null, 0)));

        // The existing pack was renamed, not replaced with a brand-new one
        assertThat(existingLoadedPack.getName()).isEqualTo("Freemium Math");
        assertThat(existingLoadedPack.getId()).isEqualTo(99L);
        // save was called for the renamed pack; no new pack was created from scratch
        verify(contentPackRepository, atLeastOnce()).save(existingLoadedPack);
    }

    @Test
    void anExistingFreemiumPackIsReusedForNewLoads() {
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.empty());
        when(classGradeRepository.findFirstByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(subjectRepository.findByClassGradeIdAndNameIgnoreCase(any(), any())).thenReturn(Optional.empty());

        ContentPack existingFreemiumPack = pack("Freemium Math", 88L);
        when(contentPackRepository.findBySubjectId(any())).thenReturn(List.of(existingFreemiumPack));

        when(quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                any(), eq(ContentTier.FREEMIUM))).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizId(any())).thenReturn(Collections.emptyList());
        when(questionRepository.findByQuizIdAndBlacklistedFalse(any())).thenReturn(Collections.emptyList());

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

    private static ContentPack pack(String name, Long id) {
        ContentPack p = new ContentPack();
        p.setName(name);
        p.setActive(true);
        p.setId(id);
        return p;
    }
}
