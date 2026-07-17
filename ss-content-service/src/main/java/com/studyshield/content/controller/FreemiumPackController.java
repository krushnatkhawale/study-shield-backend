package com.studyshield.content.controller;

import com.studyshield.content.dto.FreemiumPackRequest;
import com.studyshield.content.dto.FreemiumPackResponse;
import com.studyshield.content.service.FreemiumPackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/freemium/packs")
public class FreemiumPackController {

    private final FreemiumPackService freemiumPackService;

    public FreemiumPackController(FreemiumPackService freemiumPackService) {
        this.freemiumPackService = freemiumPackService;
    }

    @PostMapping
    public ResponseEntity<FreemiumPackResponse> issue(@Valid @RequestBody FreemiumPackRequest request) {
        FreemiumPackResponse body = freemiumPackService.issue(request);
        // 200 if already existed is fine; service always returns full payload. Use 201 for simplicity on create path.
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/{packId}")
    public ResponseEntity<FreemiumPackResponse> getById(@PathVariable Long packId) {
        return ResponseEntity.ok(freemiumPackService.getById(packId));
    }
}
