package com.studyshield.studyshield.feedback.controller;

import com.studyshield.studyshield.feedback.dto.QuestionFeedbackRequest;
import com.studyshield.studyshield.feedback.dto.QuestionFeedbackResponse;
import com.studyshield.studyshield.feedback.service.QuestionFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionFeedbackController {

    private final QuestionFeedbackService feedbackService;

    public QuestionFeedbackController(QuestionFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PutMapping("/{id}/feedback")
    public ResponseEntity<QuestionFeedbackResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody QuestionFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.submit(id, currentAccountId(), request));
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<QuestionFeedbackResponse> get(
            @PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getForUser(id, currentAccountId()));
    }

    private Long currentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        return Long.parseLong(userId);
    }
}
