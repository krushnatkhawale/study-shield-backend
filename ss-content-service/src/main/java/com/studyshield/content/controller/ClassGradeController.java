package com.studyshield.content.controller;

import com.studyshield.content.dto.ClassGradeRequest;
import com.studyshield.content.dto.ClassGradeResponse;
import com.studyshield.content.service.ClassGradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-grades")
public class ClassGradeController {

    private final ClassGradeService classGradeService;

    public ClassGradeController(ClassGradeService classGradeService) {
        this.classGradeService = classGradeService;
    }

    @PostMapping
    public ResponseEntity<ClassGradeResponse> create(@Valid @RequestBody ClassGradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classGradeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassGradeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classGradeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClassGradeResponse>> getAll() {
        return ResponseEntity.ok(classGradeService.getAll());
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<ClassGradeResponse>> getByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(classGradeService.getByBoardId(boardId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassGradeResponse> update(@PathVariable Long id, @Valid @RequestBody ClassGradeRequest request) {
        return ResponseEntity.ok(classGradeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classGradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
