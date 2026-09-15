package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.OfferingRequest;
import com.studyshield.studyshield.content.dto.OfferingResponse;
import com.studyshield.studyshield.content.service.OfferingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offerings")
public class OfferingController {

    private final OfferingService service;

    public OfferingController(OfferingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OfferingResponse> create(@Valid @RequestBody OfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OfferingResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/board-class/{boardClassId}")
    public ResponseEntity<List<OfferingResponse>> getByBoardClassId(@PathVariable Long boardClassId) {
        return ResponseEntity.ok(service.getByBoardClassId(boardClassId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferingResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody OfferingRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
