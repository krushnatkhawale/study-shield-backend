package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {
    boolean existsBySlug(String slug);
    boolean existsByOrdinal(int ordinal);
    Optional<ClassLevel> findByOrdinal(int ordinal);
    Optional<ClassLevel> findBySlug(String slug);
    List<ClassLevel> findAllByOrderByOrdinalAsc();
}
