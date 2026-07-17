package com.studyshield.quiz.controller;

import com.studyshield.quiz.dto.QuizAttemptRequest;
import com.studyshield.quiz.dto.QuizAttemptResponse;
import com.studyshield.quiz.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @PostMapping
    public ResponseEntity<QuizAttemptResponse> create(@Valid @RequestBody QuizAttemptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizAttemptService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttemptResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quizAttemptService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizAttemptResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(quizAttemptService.getByUserId(userId));
    }

    @GetMapping("/child/{childProfileId}")
    public ResponseEntity<List<QuizAttemptResponse>> getByChildProfileId(@PathVariable Long childProfileId) {
        return ResponseEntity.ok(quizAttemptService.getByChildProfileId(childProfileId));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<QuizAttemptResponse> complete(@PathVariable Long id, @RequestParam int correctAnswers) {
        return ResponseEntity.ok(quizAttemptService.complete(id, correctAnswers));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizAttemptService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
