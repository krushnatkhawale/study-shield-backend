package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.BoardClassRequest;
import com.studyshield.studyshield.content.dto.BoardClassResponse;
import com.studyshield.studyshield.content.service.BoardClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/board-classes")
public class BoardClassController {

    private final BoardClassService service;

    public BoardClassController(BoardClassService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BoardClassResponse> create(@Valid @RequestBody BoardClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardClassResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BoardClassResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<BoardClassResponse>> getByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(service.getByBoardId(boardId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardClassResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody BoardClassRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
