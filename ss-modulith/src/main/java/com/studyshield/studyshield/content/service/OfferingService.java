package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.BoardClassSubjectResponse;
import com.studyshield.studyshield.content.dto.OfferingRequest;
import com.studyshield.studyshield.content.entity.BoardClassSubject;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read/ensure endpoints for board_class_subject offerings. Offerings are the anchor for
 * all content: {@code (board + class ordinal + global subject)}.
 */
@Service
@Transactional(readOnly = true)
public class OfferingService {

    private final BoardClassSubjectRepository boardClassSubjectRepository;
    private final AcademicCatalogResolver catalogResolver;

    public OfferingService(BoardClassSubjectRepository boardClassSubjectRepository,
                           AcademicCatalogResolver catalogResolver) {
        this.boardClassSubjectRepository = boardClassSubjectRepository;
        this.catalogResolver = catalogResolver;
    }

    public BoardClassSubjectResponse getById(Long id) {
        return mapToResponse(boardClassSubjectRepository.findById(id)
                .orElseThrow(() -> new com.studyshield.studyshield.common.exception
                        .ResourceNotFoundException("Offering", id)));
    }

    public List<BoardClassSubjectResponse> getByBoardClassId(Long boardClassId) {
        return boardClassSubjectRepository
                .findByBoardClass_IdOrderBySubject_DisplayOrderAscSubject_IdAsc(boardClassId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /** Offerings for a board + class name (falls back to the generic ALL board's offerings). */
    public List<BoardClassSubjectResponse> resolve(String boardCode, String className) {
        var boardClass = catalogResolver.resolveBoardClass(
                AcademicCatalogResolver.normalizeBoardCode(boardCode), className);
        return catalogResolver.resolveOfferings(
                        AcademicCatalogResolver.normalizeBoardCode(boardCode),
                        boardClass.getClassLevel().getOrdinal())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BoardClassSubjectResponse ensure(OfferingRequest request) {
        BoardClassSubject offering = catalogResolver.resolveOrCreateOffering(
                request.boardCode(), request.className(), request.subject());
        return mapToResponse(offering);
    }

    private BoardClassSubjectResponse mapToResponse(BoardClassSubject offering) {
        return new BoardClassSubjectResponse(
                offering.getId(),
                offering.getBoardClass().getId(),
                offering.getBoardClass().getBoard().getCode(),
                offering.getBoardClass().getClassLevel().getOrdinal(),
                offering.getBoardClass().getDisplayName(),
                offering.getSubject().getId(),
                offering.getSubject().getCode(),
                offering.getSubject().getName(),
                offering.getCreatedAt(),
                offering.getUpdatedAt()
        );
    }
}