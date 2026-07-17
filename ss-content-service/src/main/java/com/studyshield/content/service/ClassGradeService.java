package com.studyshield.content.service;

import com.studyshield.content.dto.ClassGradeRequest;
import com.studyshield.content.dto.ClassGradeResponse;
import com.studyshield.content.entity.Board;
import com.studyshield.content.entity.ClassGrade;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.BoardRepository;
import com.studyshield.content.repository.ClassGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClassGradeService {

    private final ClassGradeRepository classGradeRepository;
    private final BoardRepository boardRepository;

    public ClassGradeService(ClassGradeRepository classGradeRepository, BoardRepository boardRepository) {
        this.classGradeRepository = classGradeRepository;
        this.boardRepository = boardRepository;
    }

    public ClassGradeResponse create(ClassGradeRequest request) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board", request.boardId()));
        ClassGrade classGrade = ClassGrade.builder()
                .gradeNumber(request.gradeNumber())
                .name(request.name())
                .description(request.description())
                .board(board)
                .build();
        ClassGrade saved = classGradeRepository.save(classGrade);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClassGradeResponse getById(Long id) {
        ClassGrade classGrade = classGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", id));
        return mapToResponse(classGrade);
    }

    @Transactional(readOnly = true)
    public List<ClassGradeResponse> getAll() {
        return classGradeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassGradeResponse> getByBoardId(Long boardId) {
        return classGradeRepository.findByBoardId(boardId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClassGradeResponse update(Long id, ClassGradeRequest request) {
        ClassGrade classGrade = classGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", id));
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board", request.boardId()));
        classGrade.setGradeNumber(request.gradeNumber());
        classGrade.setName(request.name());
        classGrade.setDescription(request.description());
        classGrade.setBoard(board);
        ClassGrade saved = classGradeRepository.save(classGrade);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        ClassGrade classGrade = classGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", id));
        classGradeRepository.delete(classGrade);
    }

    private ClassGradeResponse mapToResponse(ClassGrade classGrade) {
        return new ClassGradeResponse(
                classGrade.getId(),
                classGrade.getGradeNumber(),
                classGrade.getName(),
                classGrade.getDescription(),
                classGrade.getBoard().getId(),
                classGrade.getBoard().getName(),
                classGrade.getCreatedAt(),
                classGrade.getUpdatedAt()
        );
    }
}
