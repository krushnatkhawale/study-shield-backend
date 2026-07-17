package com.studyshield.content.repository;

import com.studyshield.content.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassGradeId(Long classGradeId);
    List<Subject> findByActiveTrue();
}
