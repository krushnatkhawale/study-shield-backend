package com.studyshield.studyshield.content.academicstructure;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test/CI source: reads {@code classpath:academic-structure/<entity>.json} instead of
 * hitting GitHub, so the real load path (parse → validate → insert → bump sequence)
 * is exercised without network access.
 */
public class ClasspathAcademicStructureSource implements AcademicStructureFileSource {

    private final AcademicDataProperties properties;

    public ClasspathAcademicStructureSource(AcademicDataProperties properties) {
        this.properties = properties;
    }

    @Override
    public String describe() {
        return "classpath:" + properties.getPath();
    }

    @Override
    public String describe(AcademicStructureEntity entity) {
        return resourcePath(entity);
    }

    @Override
    public String fetchJson(AcademicStructureEntity entity) {
        String path = resourcePath(entity);
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new AcademicStructureConflictException(
                    "Classpath source file not found: " + path,
                    java.util.Map.of("entity", entity.key(), "source", path));
        }
    }

    private String resourcePath(AcademicStructureEntity entity) {
        return properties.getPath() + "/" + entity.key() + ".json";
    }
}
