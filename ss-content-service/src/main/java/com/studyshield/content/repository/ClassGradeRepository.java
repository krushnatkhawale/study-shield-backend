package com.studyshield.content.repository;

import com.studyshield.content.entity.ClassGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassGradeRepository extends JpaRepository<ClassGrade, Long> {
    List<ClassGrade> findByBoardId(Long boardId);

    Optional<ClassGrade> findFirstByNameIgnoreCase(String name);

    List<ClassGrade> findByNameIgnoreCase(String name);
}
