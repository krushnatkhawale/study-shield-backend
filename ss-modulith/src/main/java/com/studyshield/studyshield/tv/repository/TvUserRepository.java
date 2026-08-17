package com.studyshield.studyshield.tv.repository;

import com.studyshield.studyshield.tv.entity.TvUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TvUserRepository extends JpaRepository<TvUser, Long> {
    Optional<TvUser> findByExternalId(String externalId);
}
