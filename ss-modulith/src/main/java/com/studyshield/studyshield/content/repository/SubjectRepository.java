package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByActiveTrue();
    Optional<Subject> findByCode(String code);
    Optional<Subject> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}