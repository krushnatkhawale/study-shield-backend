package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.ClassLevelResponse;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassLevelService {

    private final ClassLevelRepository classLevelRepository;

    public ClassLevelService(ClassLevelRepository classLevelRepository) {
        this.classLevelRepository = classLevelRepository;
    }

    public List<ClassLevelResponse> getAll() {
        return classLevelRepository.findAllByOrderByOrdinalAsc().stream()
                .map(l -> new ClassLevelResponse(
                        l.getId(), l.getOrdinal(), l.getSlug(), l.getCanonicalName(),
                        l.getAgeMinYears(), l.getAgeMaxYears(), l.getStage(), l.getNotes(),
                        l.getCreatedAt(), l.getUpdatedAt()))
                .toList();
    }

    public ClassLevelResponse getByOrdinal(int ordinal) {
        return classLevelRepository.findByOrdinal(ordinal)
                .map(l -> new ClassLevelResponse(
                        l.getId(), l.getOrdinal(), l.getSlug(), l.getCanonicalName(),
                        l.getAgeMinYears(), l.getAgeMaxYears(), l.getStage(), l.getNotes(),
                        l.getCreatedAt(), l.getUpdatedAt()))
                .orElseThrow(() -> new com.studyshield.studyshield.common.exception
                        .ResourceNotFoundException("ClassLevel", (long) ordinal));
    }
}