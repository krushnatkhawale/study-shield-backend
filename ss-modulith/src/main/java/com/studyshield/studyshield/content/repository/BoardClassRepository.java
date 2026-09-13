package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.BoardClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardClassRepository extends JpaRepository<BoardClass, Long> {
    Optional<BoardClass> findByBoard_IdAndClassLevel_Ordinal(Long boardId, int ordinal);
    @Query("""
            SELECT bc FROM BoardClass bc
            WHERE bc.board.code = :boardCode
              AND LOWER(bc.displayName) = LOWER(:displayName)
            ORDER BY bc.classLevel.ordinal ASC
            """)
    List<BoardClass> findByBoardBoardCodeAndDisplayNameEnumerable(
            @Param("boardCode") String boardCode, @Param("displayName") String displayName);

    List<BoardClass> findByBoard_IdOrderByClassLevel_OrdinalAsc(Long boardId);
    boolean existsByBoard_IdAndClassLevel_Ordinal(Long boardId, int ordinal);
    boolean existsByBoard_CodeAndClassLevel_Ordinal(String boardCode, int ordinal);
}