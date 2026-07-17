package com.studyshield.content.service;

import com.studyshield.content.dto.ContentPackRequest;
import com.studyshield.content.dto.ContentPackResponse;
import com.studyshield.content.entity.ContentPack;
import com.studyshield.content.entity.Subject;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.ContentPackRepository;
import com.studyshield.content.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentPackService {

    private final ContentPackRepository contentPackRepository;
    private final SubjectRepository subjectRepository;

    public ContentPackService(ContentPackRepository contentPackRepository, SubjectRepository subjectRepository) {
        this.contentPackRepository = contentPackRepository;
        this.subjectRepository = subjectRepository;
    }

    public ContentPackResponse create(ContentPackRequest request) {
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        ContentPack contentPack = ContentPack.builder()
                .name(request.name())
                .description(request.description())
                .subject(subject)
                .version(request.version())
                .active(request.active())
                .build();
        ContentPack saved = contentPackRepository.save(contentPack);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContentPackResponse getById(Long id) {
        ContentPack contentPack = contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id));
        return mapToResponse(contentPack);
    }

    @Transactional(readOnly = true)
    public List<ContentPackResponse> getAll() {
        return contentPackRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentPackResponse> getBySubjectId(Long subjectId) {
        return contentPackRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContentPackResponse update(Long id, ContentPackRequest request) {
        ContentPack contentPack = contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id));
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        contentPack.setName(request.name());
        contentPack.setDescription(request.description());
        contentPack.setSubject(subject);
        contentPack.setVersion(request.version());
        contentPack.setActive(request.active());
        ContentPack saved = contentPackRepository.save(contentPack);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        ContentPack contentPack = contentPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", id));
        contentPackRepository.delete(contentPack);
    }

    private ContentPackResponse mapToResponse(ContentPack contentPack) {
        return new ContentPackResponse(
                contentPack.getId(),
                contentPack.getName(),
                contentPack.getDescription(),
                contentPack.getSubject().getId(),
                contentPack.getSubject().getName(),
                contentPack.getVersion(),
                contentPack.isActive(),
                contentPack.getCreatedAt(),
                contentPack.getUpdatedAt()
        );
    }
}
