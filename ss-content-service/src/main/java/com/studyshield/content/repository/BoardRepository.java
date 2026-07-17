package com.studyshield.content.repository;

import com.studyshield.content.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByCode(String code);
    boolean existsByCode(String code);
}
