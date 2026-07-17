package com.studyshield.user.repository;

import com.studyshield.user.entity.ChildProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildProfileRepository extends JpaRepository<ChildProfile, Long> {
    List<ChildProfile> findByUserId(Long userId);
}
