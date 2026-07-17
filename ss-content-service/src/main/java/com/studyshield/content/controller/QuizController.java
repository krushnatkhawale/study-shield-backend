package com.studyshield.content.controller;

import com.studyshield.content.dto.QuizRequest;
import com.studyshield.content.dto.QuizResponse;
import com.studyshield.content.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizResponse> create(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAll() {
        return ResponseEntity.ok(quizService.getAll());
    }

    @GetMapping("/content-pack/{contentPackId}")
    public ResponseEntity<List<QuizResponse>> getByContentPackId(@PathVariable Long contentPackId) {
        return ResponseEntity.ok(quizService.getByContentPackId(contentPackId));
    }

    /**
     * Download payload for mobile freemium/premium cache: quizzes with active questions
     * (options + correctAnswers) — matches app play model.
     */
    @GetMapping("/content-pack/{contentPackId}/download")
    public ResponseEntity<List<QuizResponse>> downloadByContentPackId(@PathVariable Long contentPackId) {
        return ResponseEntity.ok(quizService.getByContentPackIdWithQuestions(contentPackId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> update(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
