package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.BoardClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardClassRepository extends JpaRepository<BoardClass, Long> {
    List<BoardClass> findByBoardId(Long boardId);
}
