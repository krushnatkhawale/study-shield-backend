package com.studyshield.tv.controller;

import com.studyshield.tv.dto.ConnectedTVRequest;
import com.studyshield.tv.dto.ConnectedTVResponse;
import com.studyshield.tv.service.ConnectedTVService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connected-tvs")
public class ConnectedTVController {

    private final ConnectedTVService connectedTVService;

    public ConnectedTVController(ConnectedTVService connectedTVService) {
        this.connectedTVService = connectedTVService;
    }

    @PostMapping
    public ResponseEntity<ConnectedTVResponse> create(@Valid @RequestBody ConnectedTVRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectedTVService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectedTVResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(connectedTVService.getById(id));
    }

    @GetMapping("/wifi-network/{wifiNetworkId}")
    public ResponseEntity<List<ConnectedTVResponse>> getByWifiNetworkId(@PathVariable Long wifiNetworkId) {
        return ResponseEntity.ok(connectedTVService.getByWifiNetworkId(wifiNetworkId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectedTVResponse> update(@PathVariable Long id, @Valid @RequestBody ConnectedTVRequest request) {
        return ResponseEntity.ok(connectedTVService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        connectedTVService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
