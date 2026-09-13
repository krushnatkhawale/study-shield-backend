package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.BoardClassRequest;
import com.studyshield.studyshield.content.dto.BoardClassResponse;
import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardClassService {

    private final BoardClassRepository boardClassRepository;
    private final BoardRepository boardRepository;
    private final AcademicCatalogResolver catalogResolver;

    public BoardClassService(BoardClassRepository boardClassRepository,
                             BoardRepository boardRepository,
                             AcademicCatalogResolver catalogResolver) {
        this.boardClassRepository = boardClassRepository;
        this.boardRepository = boardRepository;
        this.catalogResolver = catalogResolver;
    }

    public BoardClassResponse getById(Long id) {
        return mapToResponse(boardClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BoardClass", id)));
    }

    public List<BoardClassResponse> getByBoardId(Long boardId) {
        return boardClassRepository.findByBoard_IdOrderByClassLevel_OrdinalAsc(boardId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BoardClassResponse create(BoardClassRequest request) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board", request.boardId()));
        var boardClass = catalogResolver.resolveOrCreateBoardClass(
                board, request.ordinal(), request.displayName());
        return mapToResponse(boardClass);
    }

    private BoardClassResponse mapToResponse(com.studyshield.studyshield.content.entity.BoardClass bc) {
        return new BoardClassResponse(
                bc.getId(),
                bc.getBoard().getId(),
                bc.getBoard().getCode(),
                bc.getBoard().getName(),
                bc.getClassLevel().getId(),
                bc.getClassLevel().getOrdinal(),
                bc.getDisplayName(),
                bc.getCreatedAt(),
                bc.getUpdatedAt()
        );
    }
}