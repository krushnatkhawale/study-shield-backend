package com.studyshield.studyshield.quizresult.controller;

import com.studyshield.studyshield.quizresult.dto.QuizResultListItem;
import com.studyshield.studyshield.quizresult.dto.QuizResultRequest;
import com.studyshield.studyshield.quizresult.dto.QuizResultResponse;
import com.studyshield.studyshield.quizresult.service.QuizResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    @PostMapping
    public ResponseEntity<QuizResultResponse> save(@Valid @RequestBody QuizResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizResultService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<QuizResultListItem>> list() {
        return ResponseEntity.ok(quizResultService.listAll());
    }

    @GetMapping("/child/{childName}")
    public ResponseEntity<List<QuizResultListItem>> listByChild(@PathVariable String childName) {
        return ResponseEntity.ok(quizResultService.listByChildName(childName));
    }
}
