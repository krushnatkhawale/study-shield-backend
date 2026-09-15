package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.ClassLevelRequest;
import com.studyshield.studyshield.content.dto.ClassLevelResponse;
import com.studyshield.studyshield.content.service.ClassLevelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-levels")
public class ClassLevelController {

    private final ClassLevelService service;

    public ClassLevelController(ClassLevelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClassLevelResponse> create(@Valid @RequestBody ClassLevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassLevelResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClassLevelResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassLevelResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody ClassLevelRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
