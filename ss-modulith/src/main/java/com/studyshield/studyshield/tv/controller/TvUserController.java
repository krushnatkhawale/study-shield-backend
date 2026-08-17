package com.studyshield.studyshield.tv.controller;

import com.studyshield.studyshield.tv.dto.TvUserRequest;
import com.studyshield.studyshield.tv.dto.TvUserResponse;
import com.studyshield.studyshield.tv.service.TvUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tv-users")
public class TvUserController {

    private final TvUserService userService;

    public TvUserController(TvUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TvUserResponse> create(@Valid @RequestBody TvUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<TvUserResponse> getByExternalId(@PathVariable String externalId) {
        return ResponseEntity.ok(userService.getByExternalId(externalId));
    }
}
