package com.studyshield.studyshield.studygoal.repository;

import com.studyshield.studyshield.studygoal.entity.StudyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyGoalRepository extends JpaRepository<StudyGoal, Long> {
    List<StudyGoal> findByChildNameOrderByIdAsc(String childName);
    List<StudyGoal> findByChildNameAndActiveTrueOrderByIdAsc(String childName);
}
