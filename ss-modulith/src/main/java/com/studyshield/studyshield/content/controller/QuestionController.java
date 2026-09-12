package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.AssignQuizRequest;
import com.studyshield.studyshield.content.dto.QuestionBankLoadItem;
import com.studyshield.studyshield.content.dto.QuestionBankLoadResponse;
import com.studyshield.studyshield.content.dto.QuestionRequest;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.service.QuestionBankLoader;
import com.studyshield.studyshield.content.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionBankLoader questionBankLoader;

    public QuestionController(QuestionService questionService, QuestionBankLoader questionBankLoader) {
        this.questionService = questionService;
        this.questionBankLoader = questionBankLoader;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<QuestionResponse>> createBulk(@Valid @RequestBody List<QuestionRequest> requests) {
        List<QuestionResponse> created = requests.stream().map(questionService::create).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/load")
    public ResponseEntity<QuestionBankLoadResponse> loadBank(@Valid @RequestBody List<QuestionBankLoadItem> items) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionBankLoader.load(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<QuestionResponse>> getRevisions(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getRevisions(id));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAll() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuestionResponse>> getByQuizId(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.getByQuizId(quizId));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<QuestionResponse>> getBySubjectId(@PathVariable Long subjectId) {
        return ResponseEntity.ok(questionService.getLatestBySubjectId(subjectId));
    }

    @PostMapping("/{id}/assign-quiz")
    public ResponseEntity<QuestionResponse> assignQuiz(
            @PathVariable Long id, @Valid @RequestBody AssignQuizRequest request) {
        return ResponseEntity.ok(questionService.assignQuiz(id, request.quizId()));
    }

    @GetMapping("/quiz/{quizId}/active")
    public ResponseEntity<List<QuestionResponse>> getActiveByQuizId(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.getActiveByQuizId(quizId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
