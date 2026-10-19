package com.studyshield.studyshield.content.academicstructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Selects the academic-structure JSON source from {@code app.academic-data.source}:
 * {@code github} (default, raw.githubusercontent.com) or {@code classpath} (tests).
 */
@Configuration
public class AcademicStructureSourceConfig {

    @Bean
    @ConditionalOnProperty(name = "app.academic-data.source", havingValue = "github", matchIfMissing = true)
    public AcademicStructureFileSource githubAcademicStructureSource(AcademicDataProperties properties) {
        return new GitHubRawAcademicStructureSource(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.academic-data.source", havingValue = "classpath")
    public AcademicStructureFileSource classpathAcademicStructureSource(AcademicDataProperties properties) {
        return new ClasspathAcademicStructureSource(properties);
    }
}
