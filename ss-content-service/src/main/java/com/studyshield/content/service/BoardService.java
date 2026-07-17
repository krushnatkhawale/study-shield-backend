package com.studyshield.content.service;

import com.studyshield.content.dto.BoardRequest;
import com.studyshield.content.dto.BoardResponse;
import com.studyshield.content.entity.Board;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public BoardResponse create(BoardRequest request) {
        if (boardRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Board code already exists: " + request.code());
        }
        Board board = Board.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .active(request.active())
                .build();
        Board saved = boardRepository.save(board);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BoardResponse getById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id));
        return mapToResponse(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> getAll() {
        return boardRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BoardResponse update(Long id, BoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id));
        board.setName(request.name());
        board.setCode(request.code());
        board.setDescription(request.description());
        board.setActive(request.active());
        Board saved = boardRepository.save(board);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id));
        boardRepository.delete(board);
    }

    private BoardResponse mapToResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getCode(),
                board.getDescription(),
                board.isActive(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
