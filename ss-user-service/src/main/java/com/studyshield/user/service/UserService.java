package com.studyshield.user.service;

import com.studyshield.user.dto.UserRequest;
import com.studyshield.user.dto.UserResponse;
import com.studyshield.user.entity.User;
import com.studyshield.user.exception.ResourceNotFoundException;
import com.studyshield.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = User.builder()
                .email(request.email())
                .password(request.password() != null ? passwordEncoder.encode(request.password()) : null)
                .name(request.name())
                .phone(request.phone())
                .role(request.role() != null ? User.UserRole.valueOf(request.role()) : User.UserRole.PARENT)
                .active(request.active())
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
    public List<UserResponse> getAllByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        if (request.role() != null) user.setRole(User.UserRole.valueOf(request.role()));
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

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getPhone(), user.getRole().name(), user.isActive(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
