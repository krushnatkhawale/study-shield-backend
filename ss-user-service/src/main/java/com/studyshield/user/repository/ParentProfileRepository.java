package com.studyshield.user.repository;

import com.studyshield.user.entity.ParentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentProfileRepository extends JpaRepository<ParentProfile, Long> {
    List<ParentProfile> findByUserId(Long userId);
    Optional<ParentProfile> findByUserIdAndIsDefaultTrue(Long userId);
    Optional<ParentProfile> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
