package com.studyshield.content.service;

import com.studyshield.content.dto.SubjectRequest;
import com.studyshield.content.dto.SubjectResponse;
import com.studyshield.content.entity.ClassGrade;
import com.studyshield.content.entity.Subject;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.ClassGradeRepository;
import com.studyshield.content.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ClassGradeRepository classGradeRepository;

    public SubjectService(SubjectRepository subjectRepository, ClassGradeRepository classGradeRepository) {
        this.subjectRepository = subjectRepository;
        this.classGradeRepository = classGradeRepository;
    }

    public SubjectResponse create(SubjectRequest request) {
        ClassGrade classGrade = classGradeRepository.findById(request.classGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", request.classGradeId()));
        Subject subject = Subject.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .classGrade(classGrade)
                .active(request.active())
                .build();
        Subject saved = subjectRepository.save(subject);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        return mapToResponse(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getByClassGradeId(Long classGradeId) {
        return subjectRepository.findByClassGradeId(classGradeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        ClassGrade classGrade = classGradeRepository.findById(request.classGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", request.classGradeId()));
        subject.setName(request.name());
        subject.setCode(request.code());
        subject.setDescription(request.description());
        subject.setClassGrade(classGrade);
        subject.setActive(request.active());
        Subject saved = subjectRepository.save(subject);
        return mapToResponse(saved);
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
                subject.getClassGrade().getId(),
                subject.getClassGrade().getName(),
                subject.isActive(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
