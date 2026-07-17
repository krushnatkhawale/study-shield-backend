package com.studyshield.quiz.repository;

import com.studyshield.quiz.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByQuizAttemptId(Long quizAttemptId);
}
