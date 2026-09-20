package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.dto.BoardClassRequest;
import com.studyshield.studyshield.content.dto.BoardClassResponse;
import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardClassService {

    private final BoardClassRepository repository;
    private final BoardRepository boardRepository;
    private final ClassLevelRepository classLevelRepository;

    public BoardClassService(BoardClassRepository repository, BoardRepository boardRepository,
                             ClassLevelRepository classLevelRepository) {
        this.repository = repository;
        this.boardRepository = boardRepository;
        this.classLevelRepository = classLevelRepository;
    }

    public BoardClassResponse create(BoardClassRequest request) {
        BoardClass entity = new BoardClass();
        entity.setBoard(board(request.boardId()));
        entity.setClassLevel(resolveLevel(request));
        entity.setDisplayName(request.displayName());
        return map(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public BoardClassResponse getById(Long id) {
        return map(find(id));
    }

    @Transactional(readOnly = true)
    public List<BoardClassResponse> getAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<BoardClassResponse> getByBoardId(Long boardId) {
        return repository.findByBoardId(boardId).stream().map(this::map).toList();
    }

    public BoardClassResponse update(Long id, BoardClassRequest request) {
        BoardClass entity = find(id);
        entity.setBoard(board(request.boardId()));
        entity.setClassLevel(resolveLevel(request));
        entity.setDisplayName(request.displayName());
        return map(repository.save(entity));
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    private BoardClass find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BoardClass", id));
    }

    private Board board(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id));
    }

    private ClassLevel resolveLevel(BoardClassRequest request) {
        if (request.classLevelId() != null) {
            return classLevel(request.classLevelId());
        }
        if (request.ordinal() != null) {
            return classLevelRepository.findByOrdinal(request.ordinal())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassLevel", String.valueOf(request.ordinal())));
        }
        throw new IllegalArgumentException("classLevelId or ordinal is required");
    }

    private ClassLevel classLevel(Long id) {
        return classLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel", id));
    }

    private BoardClassResponse map(BoardClass entity) {
        return new BoardClassResponse(
                entity.getId(),
                entity.getBoard().getId(),
                entity.getClassLevel().getId(),
                entity.getDisplayName(),
                entity.getClassLevel().getOrdinal(),
                entity.getBoard().getCode(),
                entity.getBoard().getName());
    }
}
