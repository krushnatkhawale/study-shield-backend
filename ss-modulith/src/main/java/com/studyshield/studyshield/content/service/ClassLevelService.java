package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.dto.ClassLevelRequest;
import com.studyshield.studyshield.content.dto.ClassLevelResponse;
import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return repository.findAll().stream().map(this::map).toList();
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
        entity.setAgeMinYears(request.ageMinYears());
        entity.setAgeMaxYears(request.ageMaxYears());
        entity.setStage(request.stage());
        entity.setNotes(request.notes());
    }

    private ClassLevelResponse map(ClassLevel entity) {
        return new ClassLevelResponse(
                entity.getId(), entity.getOrdinal(), entity.getSlug(),
                entity.getCanonicalName(), entity.getAgeMinYears(),
                entity.getAgeMaxYears(), entity.getStage(), entity.getNotes());
    }
}
