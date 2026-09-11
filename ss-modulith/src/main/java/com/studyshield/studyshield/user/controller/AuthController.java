package com.studyshield.studyshield.user.controller;

import com.studyshield.studyshield.user.dto.ParentProfileResponse;
import com.studyshield.studyshield.user.dto.ParentSummary;
import com.studyshield.studyshield.user.dto.UserResponse;
import com.studyshield.studyshield.user.dto.auth.AuthResponse;
import com.studyshield.studyshield.user.dto.auth.AdminPasswordResetRequest;
import com.studyshield.studyshield.user.dto.auth.GuestAuthRequest;
import com.studyshield.studyshield.user.dto.auth.SignInRequest;
import com.studyshield.studyshield.user.dto.auth.SignUpRequest;
import com.studyshield.studyshield.user.dto.auth.ValidationResponse;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.user.security.JwtProvider;
import com.studyshield.studyshield.user.service.ChildProfileService;
import com.studyshield.studyshield.user.service.ParentProfileService;
import com.studyshield.studyshield.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ParentProfileService parentProfileService;
    private final ChildProfileService childProfileService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, ParentProfileService parentProfileService,
                          ChildProfileService childProfileService,
                          JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.parentProfileService = parentProfileService;
        this.childProfileService = childProfileService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        if (userService.existsByEmail(request.loginId())) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.error("EMAIL_EXISTS", "Email already registered: " + request.loginId()));
        }

        String name = request.name() != null ? request.name() : request.loginId();

        UserResponse saved = userService.create(
                new com.studyshield.studyshield.user.dto.UserRequest(
                        request.loginId(),
                        request.password(),
                        name,
                        null,
                        User.UserRole.PARENT.name(),
                        null,
                        true));

        ParentProfileResponse defaultParent = parentProfileService.createDefault(saved.id(), saved.name());
        childProfileService.createDefault(saved.id());

        String token = jwtProvider.generateToken(null, saved.id(), saved.email(), saved.role(),
                User.UserType.MOBILE.name());

        List<ParentSummary> parents = List.of(
                new ParentSummary(defaultParent.id().toString(), defaultParent.name()));

        return ResponseEntity.ok(AuthResponse.success(
                saved.id().toString(),
                saved.email(),
                token,
                defaultParent.id().toString(),
                defaultParent.name(),
                false,
                parents));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            User user = userService.findByEmail(request.loginId());

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("INVALID_CREDENTIALS", "Invalid login ID or password"));
            }

            if (user.getUserType() == User.UserType.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        AuthResponse.error("ACCOUNT_NOT_MOBILE",
                                "This is an admin account; sign in through the admin app"));
            }

            String token = jwtProvider.generateToken(null, user.getId(), user.getEmail(),
                    user.getRole().name(), user.getUserType().name());

            List<ParentProfileResponse> parentProfiles = parentProfileService.getByUserId(user.getId());
            List<ParentSummary> parents = parentProfiles.stream()
                    .map(p -> new ParentSummary(p.id().toString(), p.name()))
                    .toList();

            boolean requiresSelection = parentProfiles.size() > 1;
            String parentId = null;
            String parentName = null;

            if (!requiresSelection && !parentProfiles.isEmpty()) {
                parentId = parentProfiles.get(0).id().toString();
                parentName = parentProfiles.get(0).name();
            }

            return ResponseEntity.ok(AuthResponse.success(
                    user.getId().toString(),
                    user.getEmail(),
                    token,
                    parentId,
                    parentName,
                    requiresSelection,
                    parents));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.error("INVALID_CREDENTIALS", "Invalid login ID or password"));
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> guestAuth(@Valid @RequestBody GuestAuthRequest request) {
        try {
            // Deterministic per-device account so a guest's quizzes and results
            // are stable across app restarts on the same installation.
            String guestEmail = "guest-" + request.deviceId() + "@guest.local";

            User user;
            if (userService.existsByEmail(guestEmail)) {
                user = userService.findByEmail(guestEmail);
            } else {
                String randomPassword = UUID.randomUUID().toString() + System.nanoTime();
                userService.create(new com.studyshield.studyshield.user.dto.UserRequest(
                        guestEmail, randomPassword, "Guest", null,
                        User.UserRole.PARENT.name(), null, true));
                user = userService.findByEmail(guestEmail);
            }

            String token = jwtProvider.generateToken(null, user.getId(), user.getEmail(),
                    user.getRole().name(), user.getUserType().name());

            return ResponseEntity.ok(AuthResponse.success(
                    user.getId().toString(),
                    user.getEmail(),
                    token,
                    null,
                    null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error("GUEST_AUTH_FAILED", "Could not create guest session"));
        }
    }

    @PostMapping("/admin-signin")
    public ResponseEntity<AuthResponse> adminSignIn(@Valid @RequestBody SignInRequest request) {
        try {
            User user = userService.findByEmail(request.loginId());

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("INVALID_CREDENTIALS", "Invalid login ID or password"));
            }

            if (user.getUserType() != User.UserType.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        AuthResponse.error("ACCOUNT_NOT_ADMIN",
                                "This is a mobile account; sign in through the mobile app"));
            }

            String token = jwtProvider.generateToken(null, user.getId(), user.getEmail(),
                    user.getRole().name(), user.getUserType().name());

            return ResponseEntity.ok(AuthResponse.success(
                    user.getId().toString(),
                    user.getEmail(),
                    token,
                    null,
                    null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.error("INVALID_CREDENTIALS", "Invalid login ID or password"));
        }
    }

    @PostMapping("/admin-reset-password")
    public ResponseEntity<AuthResponse> adminResetPassword(
            @Valid @RequestBody AdminPasswordResetRequest request) {
        try {
            userService.resetAdminPassword(request.email(), request.newPassword());
            return ResponseEntity.ok(AuthResponse.success(null, null, null, null, null,
                    false, List.of()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.error("RESET_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error("RESET_FAILED", "Password reset failed"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // Return OK with valid=false instead of 401, so the mobile app doesn't
            // clear the local session immediately. This allows it to trust the
            // cached state when offline/flaky, or try a re-login only when sure.
            return ResponseEntity.ok(ValidationResponse.error("INVALID_TOKEN", "Invalid or expired token"));
        }

        String userId = (String) authentication.getPrincipal();

        try {
            UserResponse user = userService.getById(Long.parseLong(userId));
            return ResponseEntity.ok(ValidationResponse.success(
                    user.id().toString(),
                    user.email(),
                    user.name()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ValidationResponse.error("INVALID_TOKEN", "Invalid or expired token"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<AuthResponse> signOut() {
        return ResponseEntity.ok(AuthResponse.success(null, null, null, null, null, false, List.of()));
    }
}
