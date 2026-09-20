package com.studyshield.studyshield.studygoal.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.quizresult.entity.QuizResult;
import com.studyshield.studyshield.quizresult.repository.QuizResultRepository;
import com.studyshield.studyshield.studygoal.controller.StudyGoalController;
import com.studyshield.studyshield.studygoal.dto.GoalProgressResponse;
import com.studyshield.studyshield.studygoal.dto.StudyGoalRequest;
import com.studyshield.studyshield.studygoal.entity.StudyGoal;
import com.studyshield.studyshield.studygoal.repository.StudyGoalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudyGoalServiceTest {

    private StudyGoal goal(Long id, String child, String type, int target, boolean active) {
        StudyGoal g = StudyGoal.builder().accountId(7L).childName(child).type(type).target(target).active(active).build();
        g.setId(id);
        return g;
    }

    private QuizResult result(String child, int score, int total, LocalDateTime completedAt) {
        return QuizResult.builder().childName(child).score(score).totalQuestions(total)
                .timeSpentSeconds(60L).completedAt(completedAt).build();
    }

    private StudyGoalService service(StudyGoalRepository repo, QuizResultRepository qr) {
        return new StudyGoalService(repo, qr);
    }

    @Test
    void crudRoundTrip() {
        StudyGoalRepository repo = mock(StudyGoalRepository.class);
        QuizResultRepository qr = mock(QuizResultRepository.class);
        StudyGoalService svc = service(repo, qr);
        StudyGoal saved = goal(1L, "Asha", "WEEKLY_QUIZZES", 5, true);
        when(repo.save(any())).thenReturn(saved);
        when(repo.findById(1L)).thenReturn(Optional.of(saved));

        var created = svc.create(new StudyGoalRequest("Asha", "WEEKLY_QUIZZES", 5, null), 7L);
        assertThat(created.childName()).isEqualTo("Asha");
        assertThat(created.active()).isTrue();
        assertThat(svc.getById(1L).target()).isEqualTo(5);

        var updated = svc.update(1L, new StudyGoalRequest("Asha", "WEEKLY_BEST", 3, false));
        assertThat(updated.type()).isEqualTo("WEEKLY_BEST");

        svc.delete(1L);

        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.getById(99L)).isInstanceOf(ResourceNotFoundException.class);

        StudyGoalController controller = new StudyGoalController(svc);
        when(repo.findAll()).thenReturn(List.of(saved));
        ResponseEntity<?> viaHttp = controller.getAll(null);
        assertThat(viaHttp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void getAllFiltersByChildName() {
        StudyGoalRepository repo = mock(StudyGoalRepository.class);
        StudyGoalService svc = service(repo, mock(QuizResultRepository.class));
        when(repo.findByChildNameOrderByIdAsc("Asha")).thenReturn(List.of(goal(1L, "Asha", "WEEKLY_QUIZZES", 5, true)));
        assertThat(svc.getAll("Asha")).hasSize(1);
    }

    @Test
    void progressCountsWeekBoundaryAndBestThreshold() {
        StudyGoalRepository repo = mock(StudyGoalRepository.class);
        QuizResultRepository qr = mock(QuizResultRepository.class);
        StudyGoalService svc = service(repo, qr);

        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime inWeek = weekStart.plusHours(1);
        LocalDateTime lastWeek = weekStart.minusSeconds(1);

        when(repo.findByChildNameAndActiveTrueOrderByIdAsc("Asha")).thenReturn(List.of(
                goal(1L, "Asha", "WEEKLY_QUIZZES", 3, true),
                goal(2L, "Asha", "WEEKLY_BEST", 2, true)));
        when(qr.findByChildNameOrderByCompletedAtDesc("Asha")).thenReturn(List.of(
                result("Asha", 8, 10, inWeek),   // 80% -> best
                result("Asha", 7, 10, inWeek),   // 70% -> not best
                result("Asha", 10, 10, inWeek),  // best
                result("Asha", 10, 10, lastWeek) // outside week, ignored
        ));

        List<GoalProgressResponse> progress = svc.progress("Asha");
        assertThat(progress).hasSize(2);
        GoalProgressResponse quizzes = progress.stream().filter(p -> p.goalId() == 1L).findFirst().orElseThrow();
        GoalProgressResponse best = progress.stream().filter(p -> p.goalId() == 2L).findFirst().orElseThrow();
        assertThat(quizzes.current()).isEqualTo(3);
        assertThat(quizzes.achieved()).isTrue();
        assertThat(best.current()).isEqualTo(2);
        assertThat(best.achieved()).isTrue();
    }

    @Test
    void progressSeedsDefaultGoalWhenNoneExist() {
        StudyGoalRepository repo = mock(StudyGoalRepository.class);
        QuizResultRepository qr = mock(QuizResultRepository.class);
        StudyGoalService svc = service(repo, qr);

        when(repo.findByChildNameAndActiveTrueOrderByIdAsc("New")).thenReturn(List.of());
        StudyGoal seeded = goal(9L, "New", "WEEKLY_QUIZZES", 5, true);
        when(repo.save(any())).thenReturn(seeded);
        when(qr.findByChildNameOrderByCompletedAtDesc("New")).thenReturn(List.of());

        List<GoalProgressResponse> progress = svc.progress("New");
        assertThat(progress).hasSize(1);
        assertThat(progress.getFirst().type()).isEqualTo("WEEKLY_QUIZZES");
        assertThat(progress.getFirst().target()).isEqualTo(5);
        assertThat(progress.getFirst().current()).isEqualTo(0);
        assertThat(progress.getFirst().achieved()).isFalse();
    }

    @Test
    void progressIgnoresInactiveGoals() {
        StudyGoalRepository repo = mock(StudyGoalRepository.class);
        QuizResultRepository qr = mock(QuizResultRepository.class);
        StudyGoalService svc = service(repo, qr);
        when(repo.findByChildNameAndActiveTrueOrderByIdAsc("Asha"))
                .thenReturn(List.of(goal(1L, "Asha", "WEEKLY_QUIZZES", 1, true)));
        when(qr.findByChildNameOrderByCompletedAtDesc("Asha")).thenReturn(List.of());
        assertThat(svc.progress("Asha")).hasSize(1);
    }
}
