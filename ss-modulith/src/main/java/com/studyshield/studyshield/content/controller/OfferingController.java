package com.studyshield.studyshield.content.controller;

import com.studyshield.studyshield.content.dto.BoardClassSubjectResponse;
import com.studyshield.studyshield.content.dto.OfferingRequest;
import com.studyshield.studyshield.content.service.OfferingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offerings")
public class OfferingController {

    private final OfferingService offeringService;

    public OfferingController(OfferingService offeringService) {
        this.offeringService = offeringService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardClassSubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offeringService.getById(id));
    }

    @GetMapping("/board-class/{boardClassId}")
    public ResponseEntity<List<BoardClassSubjectResponse>> getByBoardClassId(@PathVariable Long boardClassId) {
        return ResponseEntity.ok(offeringService.getByBoardClassId(boardClassId));
    }

    /** List offerings for a board + class name (falls back to the ALL board's offerings). */
    @GetMapping
    public ResponseEntity<List<BoardClassSubjectResponse>> resolve(
            @RequestParam(defaultValue = "ALL") String boardCode,
            @RequestParam String className) {
        return ResponseEntity.ok(offeringService.resolve(boardCode, className));
    }

    @PostMapping
    public ResponseEntity<BoardClassSubjectResponse> ensure(@Valid @RequestBody OfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offeringService.ensure(request));
    }
}