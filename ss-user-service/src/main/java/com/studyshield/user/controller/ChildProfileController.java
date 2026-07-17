package com.studyshield.user.controller;

import com.studyshield.user.dto.ChildProfileRequest;
import com.studyshield.user.dto.ChildProfileResponse;
import com.studyshield.user.service.ChildProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/children")
public class ChildProfileController {

    private final ChildProfileService childProfileService;

    public ChildProfileController(ChildProfileService childProfileService) {
        this.childProfileService = childProfileService;
    }

    @PostMapping
    public ResponseEntity<ChildProfileResponse> create(@Valid @RequestBody ChildProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(childProfileService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChildProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(childProfileService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChildProfileResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(childProfileService.getByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChildProfileResponse> update(@PathVariable Long id, @Valid @RequestBody ChildProfileRequest request) {
        return ResponseEntity.ok(childProfileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        childProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
