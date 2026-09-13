package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.ContentPackRequest;
import com.studyshield.studyshield.content.dto.ContentPackResponse;
import com.studyshield.studyshield.content.entity.BoardClassSubject;
import com.studyshield.studyshield.content.entity.ContentPack;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.ContentPackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Content packs anchor to a {@link BoardClassSubject} offering (board + class ordinal +
 * global subject) — never to a bare subject, and never to a display name.
 */
@Service
@Transactional
public class ContentPackService {

    private final ContentPackRepository contentPackRepository;
    private final BoardClassSubjectRepository boardClassSubjectRepository;

    public ContentPackService(ContentPackRepository contentPackRepository,
                              BoardClassSubjectRepository boardClassSubjectRepository) {
        this.contentPackRepository = contentPackRepository;
        this.boardClassSubjectRepository = boardClassSubjectRepository;
    }

    public ContentPackResponse create(ContentPackRequest request) {
        BoardClassSubject offering = boardClassSubjectRepository.findById(request.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException("Offering", request.offeringId()));
        ContentPack contentPack = ContentPack.builder()
                .name(request.name())
                .description(request.description())
                .offering(offering)
                .version(request.version())
                .active(request.active())
                .packType(request.packType() != null ? request.packType() : com.studyshield.studyshield.content.entity.ContentTier.FREEMIUM)
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .build();
        return mapToResponse(contentPackRepository.save(contentPack));
    }

    @Transactional(readOnly = true)
    public ContentPackResponse getById(Long id) {
        return mapToResponse(contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id)));
    }

    @Transactional(readOnly = true)
    public List<ContentPackResponse> getAll() {
        return contentPackRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentPackResponse> getByOfferingId(Long offeringId) {
        return contentPackRepository.findByOfferingId(offeringId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContentPackResponse update(Long id, ContentPackRequest request) {
        ContentPack contentPack = contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id));
        BoardClassSubject offering = boardClassSubjectRepository.findById(request.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException("Offering", request.offeringId()));
        contentPack.setName(request.name());
        contentPack.setDescription(request.description());
        contentPack.setOffering(offering);
        contentPack.setVersion(request.version());
        contentPack.setActive(request.active());
        contentPack.setPackType(request.packType() != null ? request.packType() : contentPack.getPackType());
        contentPack.setValidFrom(request.validFrom());
        contentPack.setValidTo(request.validTo());
        return mapToResponse(contentPackRepository.save(contentPack));
    }

    public void delete(Long id) {
        ContentPack contentPack = contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id));
        contentPackRepository.delete(contentPack);
    }

    private ContentPackResponse mapToResponse(ContentPack contentPack) {
        BoardClassSubject offering = contentPack.getOffering();
        return new ContentPackResponse(
                contentPack.getId(),
                contentPack.getName(),
                contentPack.getDescription(),
                offering.getId(),
                offering.getBoardClass().getBoard().getCode(),
                offering.getBoardClass().getClassLevel().getOrdinal(),
                offering.getBoardClass().getDisplayName(),
                offering.getSubject().getCode(),
                offering.getSubject().getName(),
                contentPack.getVersion(),
                contentPack.isActive(),
                contentPack.getPackType(),
                contentPack.getValidFrom(),
                contentPack.getValidTo(),
                contentPack.getCreatedAt(),
                contentPack.getUpdatedAt()
        );
    }
}