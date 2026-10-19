package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByIso2(String iso2);
    boolean existsByIso2(String iso2);
}