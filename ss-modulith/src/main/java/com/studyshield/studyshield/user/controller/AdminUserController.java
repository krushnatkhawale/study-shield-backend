package com.studyshield.studyshield.user.controller;

import com.studyshield.studyshield.user.dto.AdminUserRequest;
import com.studyshield.studyshield.user.dto.UserRequest;
import com.studyshield.studyshield.user.dto.UserResponse;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Manages ADMIN-type accounts (the "users from the admin app"). Only reachable
 * by ADMIN users (enforced in SecurityConfig). Mobile accounts are managed via
 * the mobile app (/api/auth/signup) and are deliberately not editable here.
 */
@RestController
@RequestMapping("/api/v1/admin-users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAllByType(User.UserType.ADMIN);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return requireAdminUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody AdminUserRequest request) {
        if (!StringUtils.hasText(request.password())) {
            throw new IllegalArgumentException("Password is required when creating an admin user");
        }
        return userService.createAdminUser(request.email(), request.password(),
                request.name(), request.phone(), request.active());
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request,
                               Authentication authentication) {
        requireAdminUser(id);
        if (isSelf(id, authentication) && !request.active()) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }
        UserRequest payload = new UserRequest(request.email(), request.password(),
                request.name(), request.phone(), User.UserRole.ADMIN.name(),
                User.UserType.ADMIN.name(), request.active());
        return userService.update(id, payload);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        requireAdminUser(id);
        if (isSelf(id, authentication)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse requireAdminUser(Long id) {
        UserResponse user = userService.getById(id);
        if (!User.UserType.ADMIN.name().equals(user.userType())) {
            throw new IllegalArgumentException("Not an admin user");
        }
        return user;
    }

    private boolean isSelf(Long id, Authentication authentication) {
        return authentication != null
                && id.toString().equals(String.valueOf(authentication.getPrincipal()));
    }
}