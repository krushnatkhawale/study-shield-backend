package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TD-1: the bundle request path resolves the class without creating rows. The read-only
 * lookup must find the same matrix entries the creating resolver would.
 */
class AcademicCatalogResolverTest {

    private BoardRepository boardRepository;
    private BoardClassRepository boardClassRepository;
    private AcademicCatalogResolver resolver;

    @BeforeEach
    void setUp() {
        boardRepository = mock(BoardRepository.class);
        ClassLevelRepository classLevelRepository = mock(ClassLevelRepository.class);
        boardClassRepository = mock(BoardClassRepository.class);
        BoardClassSubjectRepository offeringRepository = mock(BoardClassSubjectRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        resolver = new AcademicCatalogResolver(boardRepository, classLevelRepository,
                boardClassRepository, offeringRepository, subjectRepository);
    }

    @Test
    void preNurseryResolvesToTheFoundationOrdinal() {
        assertThat(resolver.ordinalForClassName("PreNursery")).isEqualTo(1);
        assertThat(resolver.ordinalForClassName("Pre Nursery")).isEqualTo(1);
        assertThat(resolver.ordinalForClassName("pre-nursery")).isEqualTo(1);
        assertThat(resolver.ordinalForClassName("Nursery")).isEqualTo(2);
    }

    @Test
    void findBoardClassReadsTheAllBoardByHeuristicOrdinalWithoutWriting() {
        Board all = board(1L, "ALL");
        BoardClass class4 = boardClass(9L, all, 8, "Class 4");
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.of(all));
        when(boardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable("ALL", "Class 4"))
                .thenReturn(List.of());
        when(boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(1L, 8))
                .thenReturn(Optional.of(class4));

        assertThat(resolver.findBoardClass("all", "Class 4")).contains(class4);
        verify(boardRepository, never()).save(any());
        verify(boardClassRepository, never()).save(any());
    }

    @Test
    void findBoardClassIsEmptyWhenTheStructureDoesNotCarryTheClassYet() {
        Board all = board(1L, "ALL");
        when(boardRepository.findByCode("ALL")).thenReturn(Optional.of(all));
        when(boardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable(any(), any()))
                .thenReturn(List.of());
        when(boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(anyLong(), anyInt()))
                .thenReturn(Optional.empty());

        assertThat(resolver.findBoardClass("all", "Class 4")).isEmpty();
        verify(boardClassRepository, never()).save(any());
    }

    private static Board board(Long id, String code) {
        Board board = new Board();
        board.setId(id);
        board.setCode(code);
        board.setMinOrdinal(1);
        board.setMaxOrdinal(16);
        return board;
    }

    private static BoardClass boardClass(Long id, Board board, int ordinal, String displayName) {
        BoardClass boardClass = new BoardClass();
        boardClass.setId(id);
        boardClass.setBoard(board);
        ClassLevel level = new ClassLevel();
        level.setOrdinal(ordinal);
        boardClass.setClassLevel(level);
        boardClass.setDisplayName(displayName);
        return boardClass;
    }
}
