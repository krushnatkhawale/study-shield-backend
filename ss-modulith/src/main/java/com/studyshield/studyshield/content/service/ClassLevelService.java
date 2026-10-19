package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.dto.ClassLevelRequest;
import com.studyshield.studyshield.content.dto.ClassLevelResponse;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.ClassLevel.Stage;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ClassLevelService {

    private final ClassLevelRepository repository;

    public ClassLevelService(ClassLevelRepository repository) {
        this.repository = repository;
    }

    public ClassLevelResponse create(ClassLevelRequest request) {
        ClassLevel entity = new ClassLevel();
        apply(entity, request);
        return map(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ClassLevelResponse getById(Long id) {
        return map(find(id));
    }

    @Transactional(readOnly = true)
    public List<ClassLevelResponse> getAll() {
        return repository.findAllByOrderByOrdinalAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public ClassLevelResponse getByOrdinal(int ordinal) {
        return map(repository.findByOrdinal(ordinal)
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel", (long) ordinal)));
    }

    public ClassLevelResponse update(Long id, ClassLevelRequest request) {
        ClassLevel entity = find(id);
        apply(entity, request);
        return map(repository.save(entity));
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    private ClassLevel find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel", id));
    }

    private void apply(ClassLevel entity, ClassLevelRequest request) {
        entity.setOrdinal(request.ordinal());
        entity.setSlug(request.slug());
        entity.setCanonicalName(request.canonicalName());
        entity.setAgeMinYears(request.ageMinYears() == null ? null : BigDecimal.valueOf(request.ageMinYears()));
        entity.setAgeMaxYears(request.ageMaxYears() == null ? null : BigDecimal.valueOf(request.ageMaxYears()));
        if (request.stage() == null || request.stage().isBlank()) {
            throw new IllegalArgumentException("stage is required");
        }
        entity.setStage(Stage.valueOf(request.stage().trim().toUpperCase(Locale.ROOT)));
        entity.setNotes(request.notes());
    }

    private ClassLevelResponse map(ClassLevel entity) {
        return new ClassLevelResponse(
                entity.getId(), entity.getOrdinal(), entity.getSlug(),
                entity.getCanonicalName(), entity.getAgeMinYears(),
                entity.getAgeMaxYears(), entity.getStage(), entity.getNotes(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
