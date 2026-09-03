package com.studyshield.studyshield.feedback.repository;

import com.studyshield.studyshield.feedback.entity.QuestionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionFeedbackRepository extends JpaRepository<QuestionFeedback, Long> {
    Optional<QuestionFeedback> findByAccountIdAndQuestionId(Long accountId, Long questionId);
}
