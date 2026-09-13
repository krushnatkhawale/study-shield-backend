package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.BoardRequest;
import com.studyshield.studyshield.content.dto.BoardResponse;
import com.studyshield.studyshield.content.entity.Board;
import com.studyshield.studyshield.content.entity.Country;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardService {

    private static final int DEFAULT_MIN_ORDINAL = 2;
    private static final int DEFAULT_MAX_ORDINAL = 16;

    private final BoardRepository boardRepository;
    private final CountryRepository countryRepository;

    public BoardService(BoardRepository boardRepository, CountryRepository countryRepository) {
        this.boardRepository = boardRepository;
        this.countryRepository = countryRepository;
    }

    public BoardResponse create(BoardRequest request) {
        if (boardRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Board code already exists: " + request.code());
        }
        Board board = Board.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .country(resolveCountry(request.countryId()))
                .minOrdinal(request.minOrdinal() > 0 ? request.minOrdinal() : DEFAULT_MIN_ORDINAL)
                .maxOrdinal(request.maxOrdinal() > 0 ? request.maxOrdinal() : DEFAULT_MAX_ORDINAL)
                .active(request.active())
                .build();
        validateRange(board);
        return mapToResponse(boardRepository.save(board));
    }

    @Transactional(readOnly = true)
    public BoardResponse getById(Long id) {
        return mapToResponse(boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id)));
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
        boardRepository.findByCode(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Board code already exists: " + request.code());
                });
        board.setName(request.name());
        board.setCode(request.code());
        board.setDescription(request.description());
        board.setCountry(resolveCountry(request.countryId()));
        if (request.minOrdinal() > 0) {
            board.setMinOrdinal(request.minOrdinal());
        }
        if (request.maxOrdinal() > 0) {
            board.setMaxOrdinal(request.maxOrdinal());
        }
        board.setActive(request.active());
        validateRange(board);
        return mapToResponse(boardRepository.save(board));
    }

    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board", id));
        boardRepository.delete(board);
    }

    private Country resolveCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country", countryId));
    }

    private void validateRange(Board board) {
        if (board.getMinOrdinal() < 1 || board.getMaxOrdinal() > 17
                || board.getMinOrdinal() > board.getMaxOrdinal()) {
            throw new IllegalArgumentException(
                    "Board ordinal range must satisfy 1 <= minOrdinal <= maxOrdinal <= 17");
        }
    }

    private BoardResponse mapToResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getCode(),
                board.getDescription(),
                board.getCountry() != null ? board.getCountry().getId() : null,
                board.getCountry() != null ? board.getCountry().getName() : null,
                board.getMinOrdinal(),
                board.getMaxOrdinal(),
                board.isActive(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}