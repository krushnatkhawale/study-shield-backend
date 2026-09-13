package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.ClassLevelResponse;
import com.studyshield.studyshield.content.service.ClassLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-levels")
public class ClassLevelController {

    private final ClassLevelService classLevelService;

    public ClassLevelController(ClassLevelService classLevelService) {
        this.classLevelService = classLevelService;
    }

    @GetMapping
    public ResponseEntity<List<ClassLevelResponse>> getAll() {
        return ResponseEntity.ok(classLevelService.getAll());
    }

    @GetMapping("/ordinal/{ordinal}")
    public ResponseEntity<ClassLevelResponse> getByOrdinal(@PathVariable int ordinal) {
        return ResponseEntity.ok(classLevelService.getByOrdinal(ordinal));
    }
}