package com.studyshield.studyshield.user.controller;

import com.studyshield.studyshield.user.dto.auth.ClaimGuestDataRequest;
import com.studyshield.studyshield.user.dto.auth.ClaimGuestDataResponse;
import com.studyshield.studyshield.user.service.GuestDataMigrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transfers quiz results, children, and attempts from a guest account
 * (identified by device id) to the authenticated user's account.
 * <p>
 * Called by the mobile app after a guest signs up, so the guest's
 * local stats can appear under the newly created account.
 */
@RestController
@RequestMapping("/api/migrate")
public class GuestDataMigrationController {

    private final GuestDataMigrationService migrationService;

    public GuestDataMigrationController(GuestDataMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/guest-data")
    public ResponseEntity<ClaimGuestDataResponse> claimGuestData(
            @Valid @RequestBody ClaimGuestDataRequest request) {
        Long targetUserId = currentUserId();
        return ResponseEntity.ok(migrationService.migrate(request.deviceId(), targetUserId));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        return Long.parseLong(userId);
    }
}
