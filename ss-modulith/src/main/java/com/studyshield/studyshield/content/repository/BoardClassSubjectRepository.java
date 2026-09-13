package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.BoardClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardClassSubjectRepository extends JpaRepository<BoardClassSubject, Long> {
    Optional<BoardClassSubject> findByBoardClass_IdAndSubject_Id(Long boardClassId, Long subjectId);
    List<BoardClassSubject> findByBoardClass_IdOrderBySubject_DisplayOrderAscSubject_IdAsc(Long boardClassId);
    boolean existsByBoardClass_IdAndSubject_Id(Long boardClassId, Long subjectId);
    long countByBoardClass_Id(Long boardClassId);

    @Query("""
            SELECT o FROM BoardClassSubject o
            WHERE o.boardClass.board.code = :boardCode
              AND o.boardClass.classLevel.ordinal = :ordinal
            ORDER BY o.subject.displayOrder ASC, o.subject.id ASC
            """)
    List<BoardClassSubject> findByBoardCodeAndOrdinal(
            @Param("boardCode") String boardCode, @Param("ordinal") int ordinal);

    @Query("""
            SELECT o FROM BoardClassSubject o
            WHERE o.boardClass.board.code = :boardCode
              AND o.boardClass.classLevel.ordinal = :ordinal
              AND o.subject.code = :subjectCode
            """)
    Optional<BoardClassSubject> findByBoardCodeAndOrdinalAndSubjectCode(
            @Param("boardCode") String boardCode,
            @Param("ordinal") int ordinal,
            @Param("subjectCode") String subjectCode);
}