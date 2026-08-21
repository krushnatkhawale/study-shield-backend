package com.studyshield.studyshield.user.service;

import com.studyshield.studyshield.user.dto.ChildProfileRequest;
import com.studyshield.studyshield.user.dto.ChildProfileResponse;
import com.studyshield.studyshield.user.entity.ChildProfile;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.user.repository.ChildProfileRepository;
import com.studyshield.studyshield.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChildProfileService {

    public static final String DEFAULT_KID_NAME = "Kid1";
    public static final String DEFAULT_KID_CLASS = "Exp";

    private final ChildProfileRepository childProfileRepository;
    private final UserRepository userRepository;

    public ChildProfileService(ChildProfileRepository childProfileRepository, UserRepository userRepository) {
        this.childProfileRepository = childProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates the default kid profile ("Kid1", class "Exp") for a newly registered account,
     * mirroring the default parent profile. The "Exp" class maps to hello-world/promo content
     * so there is always something safe to run before real kid details are provided.
     */
    public ChildProfileResponse createDefault(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        ChildProfile cp = ChildProfile.builder()
                .name(DEFAULT_KID_NAME)
                .user(user)
                .studentClass(DEFAULT_KID_CLASS)
                .active(true)
                .build();
        return mapToResponse(childProfileRepository.save(cp));
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
