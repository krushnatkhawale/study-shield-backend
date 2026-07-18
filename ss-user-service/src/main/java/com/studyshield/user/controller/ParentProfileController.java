package com.studyshield.user.controller;

import com.studyshield.user.dto.ParentProfileRequest;
import com.studyshield.user.dto.ParentProfileResponse;
import com.studyshield.user.service.ParentProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parents")
public class ParentProfileController {

    private final ParentProfileService service;

    public ParentProfileController(ParentProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ParentProfileResponse>> listParents() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ParentProfileResponse> createParent(
            @Valid @RequestBody ParentProfileRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParentProfileResponse> getParent(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.getByIdAndUserId(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParentProfileResponse> updateParent(
            @PathVariable Long id,
            @Valid @RequestBody ParentProfileRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.update(id, userId, request));
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
