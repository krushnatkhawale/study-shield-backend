package com.studyshield.tv.controller;

import com.studyshield.tv.dto.WifiNetworkRequest;
import com.studyshield.tv.dto.WifiNetworkResponse;
import com.studyshield.tv.service.WifiNetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wifi-networks")
public class WifiNetworkController {

    private final WifiNetworkService wifiNetworkService;

    public WifiNetworkController(WifiNetworkService wifiNetworkService) {
        this.wifiNetworkService = wifiNetworkService;
    }

    @PostMapping
    public ResponseEntity<WifiNetworkResponse> create(@Valid @RequestBody WifiNetworkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wifiNetworkService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WifiNetworkResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(wifiNetworkService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WifiNetworkResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(wifiNetworkService.getByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wifiNetworkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
