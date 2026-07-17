package com.studyshield.content.repository;

import com.studyshield.content.entity.FreemiumPack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreemiumPackRepository extends JpaRepository<FreemiumPack, Long> {
    Optional<FreemiumPack> findByIdempotencyKey(String idempotencyKey);
}
