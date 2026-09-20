package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {
    boolean existsBySlug(String slug);
    boolean existsByOrdinal(Integer ordinal);
    Optional<ClassLevel> findByOrdinal(Integer ordinal);
}
