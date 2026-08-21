package com.studyshield.studyshield.quizresult.controller;

import com.studyshield.studyshield.quizresult.dto.QuizResultListItem;
import com.studyshield.studyshield.quizresult.dto.QuizResultRequest;
import com.studyshield.studyshield.quizresult.dto.QuizResultResponse;
import com.studyshield.studyshield.quizresult.service.QuizResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizResultService.save(request, currentAccountId()));
    }

    @GetMapping
    public ResponseEntity<List<QuizResultListItem>> list() {
        return ResponseEntity.ok(quizResultService.listAll(currentAccountId()));
    }

    @GetMapping("/child/{childName}")
    public ResponseEntity<List<QuizResultListItem>> listByChild(@PathVariable String childName) {
        return ResponseEntity.ok(quizResultService.listByChildName(currentAccountId(), childName));
    }

    private Long currentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        return Long.parseLong(userId);
    }
}
