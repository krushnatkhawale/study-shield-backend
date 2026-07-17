package com.studyshield.content.repository;

import com.studyshield.content.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByContentPackId(Long contentPackId);
    List<Quiz> findByActiveTrue();

    List<Quiz> findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
            Long contentPackId,
            com.studyshield.content.entity.ContentTier contentTier
    );
}
