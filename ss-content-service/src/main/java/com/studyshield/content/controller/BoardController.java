package com.studyshield.content.controller;

import com.studyshield.content.dto.BoardRequest;
import com.studyshield.content.dto.BoardResponse;
import com.studyshield.content.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAll() {
        return ResponseEntity.ok(boardService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(@PathVariable Long id, @Valid @RequestBody BoardRequest request) {
        return ResponseEntity.ok(boardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
