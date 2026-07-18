package com.studyshield.user.service;

import com.studyshield.user.dto.ParentProfileRequest;
import com.studyshield.user.dto.ParentProfileResponse;
import com.studyshield.user.entity.ParentProfile;
import com.studyshield.user.exception.ResourceNotFoundException;
import com.studyshield.user.repository.ParentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ParentProfileService {

    private final ParentProfileRepository repository;

    public ParentProfileService(ParentProfileRepository repository) {
        this.repository = repository;
    }

    public ParentProfileResponse createDefault(Long userId, String name) {
        ParentProfile profile = ParentProfile.builder()
                .userId(userId)
                .name(name)
                .type("ACCOUNT_HOLDER")
                .isDefault(true)
                .active(true)
                .build();
        return mapToResponse(repository.save(profile));
    }

    public ParentProfileResponse create(Long userId, ParentProfileRequest request) {
        ParentProfile profile = ParentProfile.builder()
                .userId(userId)
                .name(request.name())
                .gender(request.gender())
                .relation(request.relation())
                .type(request.type() != null ? request.type() : "ACCOUNT_HOLDER")
                .isDefault(false)
                .active(true)
                .build();
        return mapToResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ParentProfileResponse> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParentProfileResponse getDefault(Long userId) {
        ParentProfile profile = repository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Default parent profile for user", userId));
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public ParentProfileResponse getByIdAndUserId(Long id, Long userId) {
        ParentProfile profile = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile", id));
        return mapToResponse(profile);
    }

    public ParentProfileResponse update(Long id, Long userId, ParentProfileRequest request) {
        ParentProfile profile = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile", id));
        if (request.name() != null) profile.setName(request.name());
        if (request.gender() != null) profile.setGender(request.gender());
        if (request.relation() != null) profile.setRelation(request.relation());
        if (request.type() != null) profile.setType(request.type());
        return mapToResponse(repository.save(profile));
    }

    public void delete(Long id, Long userId) {
        ParentProfile profile = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile", id));
        repository.delete(profile);
    }

    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return repository.countByUserId(userId);
    }

    private ParentProfileResponse mapToResponse(ParentProfile p) {
        return new ParentProfileResponse(
                p.getId(), p.getUserId(), p.getName(), p.getGender(),
                p.getRelation(), p.getType(), p.isDefault(), p.isActive(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
