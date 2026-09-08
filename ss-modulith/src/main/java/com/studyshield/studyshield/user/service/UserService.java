package com.studyshield.studyshield.user.service;

import com.studyshield.studyshield.user.dto.UserRequest;
import com.studyshield.studyshield.user.dto.UserResponse;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        User.UserRole role = request.role() != null
                ? User.UserRole.valueOf(request.role()) : User.UserRole.PARENT;
        User.UserType userType = resolveUserType(request.userType(), role);
        User user = User.builder()
                .email(request.email())
                .password(encodeIfPresent(request.password()))
                .name(request.name())
                .phone(request.phone())
                .role(role)
                .userType(userType)
                .active(request.active())
                .build();
        return mapToResponse(userRepository.save(user));
    }

    /** Create an administrator account. The type/role contract is fixed to ADMIN. */
    public UserResponse createAdminUser(String email, String password, String name, String phone, boolean active) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        User user = User.builder()
                .email(email)
                .password(encodeIfPresent(password))
                .name(name != null ? name : email)
                .phone(phone)
                .role(User.UserRole.ADMIN)
                .userType(User.UserType.ADMIN)
                .active(active)
                .build();
        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllByType(User.UserType type) {
        return userRepository.findAll().stream()
                .filter(u -> u.getUserType() == type)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        User.UserRole role = request.role() != null
                ? User.UserRole.valueOf(request.role()) : user.getRole();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(role);
        user.setUserType(resolveUserType(request.userType(), role));
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setActive(request.active());
        return mapToResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public long countByType(User.UserType type) {
        return userRepository.findAll().stream().filter(u -> u.getUserType() == type).count();
    }

    /**
     * A user's type and role are a single contract: ADMIN users are role ADMIN,
     * MOBILE users are role PARENT. This keeps the two app audiences strictly apart.
     */
    private User.UserType resolveUserType(String userType, User.UserRole role) {
        User.UserType resolved = userType != null
                ? User.UserType.valueOf(userType)
                : (role == User.UserRole.ADMIN ? User.UserType.ADMIN : User.UserType.MOBILE);
        boolean consistent = (resolved == User.UserType.ADMIN && role == User.UserRole.ADMIN)
                || (resolved == User.UserType.MOBILE && role == User.UserRole.PARENT);
        if (!consistent) {
            throw new IllegalArgumentException(
                    "User type ADMIN requires role ADMIN; user type MOBILE requires role PARENT");
        }
        return resolved;
    }

    /**
     * Resets the password for an ADMIN account. Throws if the account
     * does not exist or is not an ADMIN type.
     */
    public void resetAdminPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with that email"));
        if (user.getUserType() != User.UserType.ADMIN) {
            throw new IllegalArgumentException("This is not an admin account");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String encodeIfPresent(String raw) {
        return raw != null ? passwordEncoder.encode(raw) : null;
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getPhone(), user.getRole().name(), user.getUserType().name(),
                user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
    }
}