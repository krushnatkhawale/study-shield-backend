package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.FreemiumRebuildResponse;
import com.studyshield.studyshield.content.dto.QuizBundleRequest;
import com.studyshield.studyshield.content.dto.QuizBundleResponse;
import com.studyshield.studyshield.content.service.QuizBundleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-bundles")
public class QuizBundleController {

    private final QuizBundleService quizBundleService;

    public QuizBundleController(QuizBundleService quizBundleService) {
        this.quizBundleService = quizBundleService;
    }

    @GetMapping
    public ResponseEntity<List<QuizBundleResponse>> getAll() {
        return ResponseEntity.ok(quizBundleService.getAll());
    }

    @PostMapping
    public ResponseEntity<QuizBundleResponse> issue(@Valid @RequestBody QuizBundleRequest request) {
        QuizBundleResponse body = quizBundleService.issue(request);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/{packId}")
    public ResponseEntity<QuizBundleResponse> getById(@PathVariable Long packId) {
        return ResponseEntity.ok(quizBundleService.getById(packId));
    }

    /**
     * Admin: delete issued (cached) bundles and seed one freemium quiz per
     * class/subject that has questions.
     */
    @PostMapping("/rebuild-catalog")
    public ResponseEntity<FreemiumRebuildResponse> rebuildCatalog() {
        return ResponseEntity.ok(quizBundleService.rebuildFreemiumCatalog());
    }
}
