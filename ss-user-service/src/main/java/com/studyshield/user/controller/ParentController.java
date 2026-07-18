package com.studyshield.user.controller;

import com.studyshield.user.dto.ParentProfileRequest;
import com.studyshield.user.dto.ParentProfileResponse;
import com.studyshield.user.dto.auth.ParentResponse;
import com.studyshield.user.service.ParentProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentProfileService service;

    public ParentController(ParentProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ParentResponse>> getAllParents() {
        Long userId = getCurrentUserId();
        List<ParentProfileResponse> profiles = service.getByUserId(userId);
        List<ParentResponse> parents = profiles.stream()
                .map(p -> ParentResponse.fromIdAndName(p.id(), p.name()))
                .toList();
        return ResponseEntity.ok(parents);
    }

    @PostMapping
    public ResponseEntity<ParentResponse> createParent(
            @RequestBody ParentProfileRequest request) {
        Long userId = getCurrentUserId();
        ParentProfileResponse saved = service.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ParentResponse.fromIdAndName(saved.id(), saved.name()));
    }

    @PutMapping("/me")
    public ResponseEntity<ParentResponse> updateCurrentUser(
            @RequestBody ParentProfileRequest request) {
        Long userId = getCurrentUserId();
        ParentProfileResponse defaultParent = service.getDefault(userId);
        ParentProfileResponse updated = service.update(defaultParent.id(), userId, request);
        return ResponseEntity.ok(ParentResponse.fromIdAndName(updated.id(), updated.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong((String) auth.getPrincipal());
    }
}
