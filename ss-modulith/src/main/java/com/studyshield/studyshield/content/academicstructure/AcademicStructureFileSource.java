package com.studyshield.studyshield.content.academicstructure;

/**
 * Where the load endpoint gets its JSON from. Two implementations:
 * {@code github} (production, raw.githubusercontent.com at the pinned ref) and
 * {@code classpath} (tests, {@code classpath:academic-structure/*.json}).
 */
public interface AcademicStructureFileSource {

    /** Human-readable identifier of the source (shown in reports and status). */
    String describe();

    /** Fetch point for one entity — a URL for github, a resource path for classpath. */
    String describe(AcademicStructureEntity entity);

    /** Raw JSON document (an array of row objects) for one entity. */
    String fetchJson(AcademicStructureEntity entity);
}
