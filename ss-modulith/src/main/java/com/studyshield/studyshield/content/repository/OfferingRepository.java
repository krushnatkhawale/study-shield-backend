package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findByBoardClassId(Long boardClassId);
    boolean existsByBoardClassIdAndSubjectId(Long boardClassId, Long subjectId);
}
