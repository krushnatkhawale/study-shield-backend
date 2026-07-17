package com.studyshield.content.controller;

import com.studyshield.content.dto.ContentPackRequest;
import com.studyshield.content.dto.ContentPackResponse;
import com.studyshield.content.service.ContentPackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/content-packs")
public class ContentPackController {

    private final ContentPackService contentPackService;

    public ContentPackController(ContentPackService contentPackService) {
        this.contentPackService = contentPackService;
    }

    @PostMapping
    public ResponseEntity<ContentPackResponse> create(@Valid @RequestBody ContentPackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentPackService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentPackResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contentPackService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ContentPackResponse>> getAll() {
        return ResponseEntity.ok(contentPackService.getAll());
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<ContentPackResponse>> getBySubjectId(@PathVariable Long subjectId) {
        return ResponseEntity.ok(contentPackService.getBySubjectId(subjectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentPackResponse> update(@PathVariable Long id, @Valid @RequestBody ContentPackRequest request) {
        return ResponseEntity.ok(contentPackService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentPackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
