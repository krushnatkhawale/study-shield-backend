package com.studyshield.studyshield.tv.service;

import com.studyshield.studyshield.tv.dto.TvUserRequest;
import com.studyshield.studyshield.tv.dto.TvUserResponse;
import com.studyshield.studyshield.tv.entity.TvUser;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.tv.repository.TvUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TvUserService {

    private final TvUserRepository userRepository;

    public TvUserService(TvUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public TvUserResponse create(TvUserRequest request) {
        TvUser user = TvUser.builder()
                .externalId(request.externalId())
                .name(request.name())
                .build();
        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TvUserResponse getByExternalId(String externalId) {
        return mapToResponse(userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", externalId)));
    }

    @Transactional(readOnly = true)
    public TvUserResponse getById(Long id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    private TvUserResponse mapToResponse(TvUser user) {
        return new TvUserResponse(user.getId(), user.getExternalId(), user.getName(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
