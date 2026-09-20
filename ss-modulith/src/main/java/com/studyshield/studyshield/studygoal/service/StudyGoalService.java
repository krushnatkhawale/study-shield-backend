package com.studyshield.studyshield.studygoal.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.quizresult.entity.QuizResult;
import com.studyshield.studyshield.quizresult.repository.QuizResultRepository;
import com.studyshield.studyshield.studygoal.dto.GoalProgressResponse;
import com.studyshield.studyshield.studygoal.dto.StudyGoalRequest;
import com.studyshield.studyshield.studygoal.dto.StudyGoalResponse;
import com.studyshield.studyshield.studygoal.entity.StudyGoal;
import com.studyshield.studyshield.studygoal.repository.StudyGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StudyGoalService {

    private final StudyGoalRepository studyGoalRepository;
    private final QuizResultRepository quizResultRepository;

    public StudyGoalService(StudyGoalRepository studyGoalRepository,
                            QuizResultRepository quizResultRepository) {
        this.studyGoalRepository = studyGoalRepository;
        this.quizResultRepository = quizResultRepository;
    }

    public StudyGoalResponse create(StudyGoalRequest request, Long accountId) {
        StudyGoal goal = StudyGoal.builder()
                .accountId(accountId)
                .childName(request.childName())
                .type(request.type())
                .target(request.target())
                .active(request.active() == null ? true : request.active())
                .build();
        return mapToResponse(studyGoalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<StudyGoalResponse> getAll(String childName) {
        List<StudyGoal> goals = childName == null
                ? studyGoalRepository.findAll()
                : studyGoalRepository.findByChildNameOrderByIdAsc(childName);
        return goals.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudyGoalResponse getById(Long id) {
        return mapToResponse(studyGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGoal", id)));
    }

    public StudyGoalResponse update(Long id, StudyGoalRequest request) {
        StudyGoal goal = studyGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGoal", id));
        goal.setChildName(request.childName());
        goal.setType(request.type());
        goal.setTarget(request.target());
        if (request.active() != null) {
            goal.setActive(request.active());
        }
        return mapToResponse(studyGoalRepository.save(goal));
    }

    public void delete(Long id) {
        StudyGoal goal = studyGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGoal", id));
        studyGoalRepository.delete(goal);
    }

    public List<GoalProgressResponse> progress(String childName) {
        List<StudyGoal> goals = studyGoalRepository.findByChildNameAndActiveTrueOrderByIdAsc(childName);
        if (goals.isEmpty()) {
            StudyGoal seeded = studyGoalRepository.save(StudyGoal.builder()
                    .childName(childName)
                    .type("WEEKLY_QUIZZES")
                    .target(5)
                    .active(true)
                    .build());
            goals = List.of(seeded);
        }
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        List<QuizResult> results = quizResultRepository.findByChildNameOrderByCompletedAtDesc(childName);
        long total = results.stream()
                .filter(r -> r.getCompletedAt() != null && !r.getCompletedAt().isBefore(weekStart))
                .count();
        long best = results.stream()
                .filter(r -> r.getCompletedAt() != null && !r.getCompletedAt().isBefore(weekStart))
                .filter(r -> r.getTotalQuestions() != null && r.getTotalQuestions() > 0
                        && r.getScore() != null
                        && r.getScore() * 100.0 / r.getTotalQuestions() >= 80.0)
                .count();
        long finalTotal = total;
        long finalBest = best;
        return goals.stream()
                .map(g -> {
                    long current = "WEEKLY_BEST".equals(g.getType()) ? finalBest : finalTotal;
                    return new GoalProgressResponse(g.getId(), g.getType(), g.getTarget(), current, current >= g.getTarget());
                })
                .toList();
    }

    private StudyGoalResponse mapToResponse(StudyGoal g) {
        return new StudyGoalResponse(
                g.getId(), g.getAccountId(), g.getChildName(), g.getType(),
                g.getTarget(), g.isActive(), g.getCreatedAt());
    }
}
