package com.studyshield.user.controller;

import com.studyshield.user.dto.ParentProfileResponse;
import com.studyshield.user.dto.ParentSummary;
import com.studyshield.user.dto.UserResponse;
import com.studyshield.user.dto.auth.AuthResponse;
import com.studyshield.user.dto.auth.SignInRequest;
import com.studyshield.user.dto.auth.SignUpRequest;
import com.studyshield.user.dto.auth.ValidationResponse;
import com.studyshield.user.entity.User;
import com.studyshield.user.security.JwtProvider;
import com.studyshield.user.service.ParentProfileService;
import com.studyshield.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ParentProfileService parentProfileService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, ParentProfileService parentProfileService,
                          JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.parentProfileService = parentProfileService;
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
                new com.studyshield.user.dto.UserRequest(
                        request.loginId(),
                        request.password(),
                        name,
                        null,
                        User.UserRole.PARENT.name(),
                        true));

        ParentProfileResponse defaultParent = parentProfileService.createDefault(saved.id(), saved.name());

        String token = jwtProvider.generateToken(null, saved.id(), saved.email(), saved.role());

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

            String token = jwtProvider.generateToken(null, user.getId(), user.getEmail(), user.getRole().name());

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

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ValidationResponse.error("INVALID_TOKEN", "Invalid or expired token"));
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
