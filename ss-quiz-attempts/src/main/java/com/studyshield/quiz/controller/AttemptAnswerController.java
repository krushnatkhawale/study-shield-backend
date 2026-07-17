package com.studyshield.quiz.controller;

import com.studyshield.quiz.dto.AttemptAnswerRequest;
import com.studyshield.quiz.dto.AttemptAnswerResponse;
import com.studyshield.quiz.service.AttemptAnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-attempts/{quizAttemptId}/answers")
public class AttemptAnswerController {

    private final AttemptAnswerService attemptAnswerService;

    public AttemptAnswerController(AttemptAnswerService attemptAnswerService) {
        this.attemptAnswerService = attemptAnswerService;
    }

    @PostMapping
    public ResponseEntity<AttemptAnswerResponse> create(@PathVariable Long quizAttemptId,
                                                         @Valid @RequestBody AttemptAnswerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptAnswerService.create(quizAttemptId, request));
    }

    @GetMapping
    public ResponseEntity<List<AttemptAnswerResponse>> getByQuizAttemptId(@PathVariable Long quizAttemptId) {
        return ResponseEntity.ok(attemptAnswerService.getByQuizAttemptId(quizAttemptId));
    }
}
