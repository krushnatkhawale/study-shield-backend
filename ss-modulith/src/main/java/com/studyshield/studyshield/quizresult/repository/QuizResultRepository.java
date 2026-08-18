package com.studyshield.studyshield.quizresult.repository;

import com.studyshield.studyshield.quizresult.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByChildNameOrderByCompletedAtDesc(String childName);
    List<QuizResult> findAllByOrderByCompletedAtDesc();
}
