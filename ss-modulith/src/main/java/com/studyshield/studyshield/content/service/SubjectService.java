package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.SubjectRequest;
import com.studyshield.studyshield.content.dto.SubjectResponse;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Global reference subjects only (one row per subject across all boards); a board/class
 * actually offering a subject is a {@code board_class_subject} row, not a subject row.
 */
@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new IllegalArgumentException("Subject code already exists: " + request.code());
        }
        Subject subject = Subject.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .active(request.active())
                .displayOrder(request.displayOrder())
                .build();
        return mapToResponse(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        return mapToResponse(subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id)));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        subjectRepository.findByCodeIgnoreCase(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Subject code already exists: " + request.code());
                });
        subject.setName(request.name());
        subject.setCode(request.code());
        subject.setDescription(request.description());
        subject.setActive(request.active());
        subject.setDisplayOrder(request.displayOrder());
        return mapToResponse(subjectRepository.save(subject));
    }

    public void delete(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        subjectRepository.delete(subject);
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getCode(),
                subject.getDescription(),
                subject.isActive(),
                subject.getDisplayOrder(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}