package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.dto.OfferingRequest;
import com.studyshield.studyshield.content.dto.OfferingResponse;
import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.entity.Offering;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.OfferingRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OfferingService {

    private final OfferingRepository repository;
    private final BoardClassRepository boardClassRepository;
    private final SubjectRepository subjectRepository;

    public OfferingService(OfferingRepository repository, BoardClassRepository boardClassRepository,
                           SubjectRepository subjectRepository) {
        this.repository = repository;
        this.boardClassRepository = boardClassRepository;
        this.subjectRepository = subjectRepository;
    }

    public OfferingResponse create(OfferingRequest request) {
        Offering entity = new Offering();
        entity.setBoardClass(boardClass(request.boardClassId()));
        entity.setSubject(subject(request.subjectId()));
        return map(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public OfferingResponse getById(Long id) {
        return map(find(id));
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getByBoardClassId(Long boardClassId) {
        return repository.findByBoardClassId(boardClassId).stream().map(this::map).toList();
    }

    public OfferingResponse update(Long id, OfferingRequest request) {
        Offering entity = find(id);
        entity.setBoardClass(boardClass(request.boardClassId()));
        entity.setSubject(subject(request.subjectId()));
        return map(repository.save(entity));
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Offering find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offering", id));
    }

    private BoardClass boardClass(Long id) {
        return boardClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BoardClass", id));
    }

    private Subject subject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private OfferingResponse map(Offering entity) {
        BoardClass bc = entity.getBoardClass();
        return new OfferingResponse(
                entity.getId(),
                bc.getId(),
                entity.getSubject().getId(),
                bc.getDisplayName(),
                bc.getClassLevel().getOrdinal(),
                bc.getBoard().getCode(),
                entity.getSubject().getCode(),
                entity.getSubject().getName());
    }
}
