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

    private final BoardClassService boardClassService;

    public BoardClassController(BoardClassService boardClassService) {
        this.boardClassService = boardClassService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardClassResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(boardClassService.getById(id));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<BoardClassResponse>> getByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardClassService.getByBoardId(boardId));
    }

    @PostMapping
    public ResponseEntity<BoardClassResponse> create(@Valid @RequestBody BoardClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardClassService.create(request));
    }
}