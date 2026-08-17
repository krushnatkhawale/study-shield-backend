package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.QuizBundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizBundleRepository extends JpaRepository<QuizBundle, Long> {
    Optional<QuizBundle> findByIdempotencyKey(String idempotencyKey);
}
