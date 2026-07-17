package com.studyshield.tv.service;

import com.studyshield.tv.dto.UserRequest;
import com.studyshield.tv.dto.UserResponse;
import com.studyshield.tv.entity.User;
import com.studyshield.tv.exception.ResourceNotFoundException;
import com.studyshield.tv.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .externalId(request.externalId())
                .name(request.name())
                .build();
        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getByExternalId(String externalId) {
        return mapToResponse(userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", externalId)));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getExternalId(), user.getName(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
