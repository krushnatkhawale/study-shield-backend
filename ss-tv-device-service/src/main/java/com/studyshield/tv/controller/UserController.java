package com.studyshield.tv.controller;

import com.studyshield.tv.dto.UserRequest;
import com.studyshield.tv.dto.UserResponse;
import com.studyshield.tv.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tv-users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<UserResponse> getByExternalId(@PathVariable String externalId) {
        return ResponseEntity.ok(userService.getByExternalId(externalId));
    }
}
