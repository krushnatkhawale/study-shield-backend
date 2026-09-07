package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassGradeId(Long classGradeId);
    List<Subject> findByClassGradeIdOrderByDisplayOrderAscIdAsc(Long classGradeId);
    List<Subject> findByActiveTrue();
    Optional<Subject> findByClassGradeIdAndNameIgnoreCase(Long classGradeId, String name);
}
