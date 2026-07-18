package com.studyshield.user.service;

import com.studyshield.user.dto.ChildProfileRequest;
import com.studyshield.user.dto.ChildProfileResponse;
import com.studyshield.user.entity.ChildProfile;
import com.studyshield.user.entity.User;
import com.studyshield.user.exception.ResourceNotFoundException;
import com.studyshield.user.repository.ChildProfileRepository;
import com.studyshield.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChildProfileService {

    private final ChildProfileRepository childProfileRepository;
    private final UserRepository userRepository;

    public ChildProfileService(ChildProfileRepository childProfileRepository, UserRepository userRepository) {
        this.childProfileRepository = childProfileRepository;
        this.userRepository = userRepository;
    }

    public ChildProfileResponse create(ChildProfileRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        ChildProfile cp = ChildProfile.builder()
                .name(request.name())
                .age(request.age())
                .user(user)
                .boardId(request.boardId())
                .classGradeId(request.classGradeId())
                .gender(request.gender())
                .birthYear(request.birthYear())
                .studentClass(request.studentClass())
                .active(request.active())
                .build();
        return mapToResponse(childProfileRepository.save(cp));
    }

    @Transactional(readOnly = true)
    public ChildProfileResponse getById(Long id) {
        return mapToResponse(childProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", id)));
    }

    @Transactional(readOnly = true)
    public List<ChildProfileResponse> getByUserId(Long userId) {
        return childProfileRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    public ChildProfileResponse update(Long id, ChildProfileRequest request) {
        ChildProfile cp = childProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", id));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        cp.setName(request.name());
        cp.setAge(request.age());
        cp.setUser(user);
        cp.setBoardId(request.boardId());
        cp.setClassGradeId(request.classGradeId());
        cp.setGender(request.gender());
        cp.setBirthYear(request.birthYear());
        cp.setStudentClass(request.studentClass());
        cp.setActive(request.active());
        return mapToResponse(childProfileRepository.save(cp));
    }

    public void delete(Long id) {
        ChildProfile cp = childProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", id));
        childProfileRepository.delete(cp);
    }

    private ChildProfileResponse mapToResponse(ChildProfile cp) {
        return new ChildProfileResponse(cp.getId(), cp.getName(), cp.getAge(),
                cp.getUser().getId(), cp.getUser().getName(),
                cp.getBoardId(), cp.getClassGradeId(),
                cp.getGender(), cp.getBirthYear(), cp.getStudentClass(),
                cp.isActive(), cp.getCreatedAt(), cp.getUpdatedAt());
    }
}
