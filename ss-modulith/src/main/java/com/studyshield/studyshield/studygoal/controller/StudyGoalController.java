package com.studyshield.studyshield.studygoal.controller;

import com.studyshield.studyshield.studygoal.dto.GoalProgressResponse;
import com.studyshield.studyshield.studygoal.dto.StudyGoalRequest;
import com.studyshield.studyshield.studygoal.dto.StudyGoalResponse;
import com.studyshield.studyshield.studygoal.service.StudyGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class StudyGoalController {

    private final StudyGoalService studyGoalService;

    public StudyGoalController(StudyGoalService studyGoalService) {
        this.studyGoalService = studyGoalService;
    }

    @PostMapping
    public ResponseEntity<StudyGoalResponse> create(@Valid @RequestBody StudyGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyGoalService.create(request, currentAccountIdOrNull()));
    }

    @GetMapping
    public ResponseEntity<List<StudyGoalResponse>> getAll(
            @RequestParam(required = false) String childName) {
        return ResponseEntity.ok(studyGoalService.getAll(childName));
    }

    @GetMapping("/progress")
    public ResponseEntity<List<GoalProgressResponse>> progress(@RequestParam String childName) {
        return ResponseEntity.ok(studyGoalService.progress(childName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studyGoalService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGoalResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody StudyGoalRequest request) {
        return ResponseEntity.ok(studyGoalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentAccountIdOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return null;
            }
            return Long.parseLong(String.valueOf(auth.getPrincipal()));
        } catch (Exception e) {
            return null;
        }
    }
}
