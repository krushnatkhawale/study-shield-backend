package com.studyshield.content.repository;

import com.studyshield.content.entity.ContentPack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentPackRepository extends JpaRepository<ContentPack, Long> {
    List<ContentPack> findBySubjectId(Long subjectId);
    List<ContentPack> findByActiveTrue();
}
